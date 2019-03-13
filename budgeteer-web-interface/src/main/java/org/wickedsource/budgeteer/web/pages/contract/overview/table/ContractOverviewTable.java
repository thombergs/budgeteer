package org.wickedsource.budgeteer.web.pages.contract.overview.table;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnumLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.contract.ContractBaseData;
import org.wickedsource.budgeteer.service.contract.ContractSortingService;
import org.wickedsource.budgeteer.service.contract.ContractTotalData;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.datelabel.DateLabel;
import org.wickedsource.budgeteer.web.components.money.BudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.components.tax.TaxBudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.tax.TaxLabelModel;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;
import org.wickedsource.budgeteer.web.pages.contract.edit.EditContractPage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class ContractOverviewTable extends Panel {

    @SpringBean
    private ContractSortingService contractSortingService;

    public ContractOverviewTable(String id) {
        super(id);
        ContractOverviewTableModel data = new ContractOverviewTableModel();
        WebMarkupContainer table = new WebMarkupContainer("table");
        table.setOutputMarkupId(true);

        // AJAX handler for post requests from JavaScript
        final AbstractDefaultAjaxBehavior behave = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(AjaxRequestTarget target) {
                IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
                // Update sorting indices
                Set<String> parameterNames = params.getParameterNames();
                StringValue value = params.getParameterValue(parameterNames.toArray()[1].toString());
                String[] parts = value.toString().split(",");

                List<ContractBaseData> contracts = data.load();

                for (int i = 0; i < parts.length; i++) {
                    long id = Long.parseLong((parts[i]));
                    Optional<ContractBaseData> item = contracts.stream().filter(a -> a.getContractId() == id).findFirst();
                    if (item.isPresent()) {
                        ContractBaseData contractBaseData = item.get();
                        contractBaseData.setSortingIndex(i);
                        contractSortingService.saveSortingIndex(contractBaseData, BudgeteerSession.get().getLoggedInUser().getId());
                    }
                }
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                String componentMarkupId = component.getMarkupId();
                String callbackUrl = getCallbackUrl().toString();
                response.render(JavaScriptHeaderItem.forScript("var componentMarkupId='" + componentMarkupId + "'; var callbackUrl='" + callbackUrl + "';", "values"));
            }
        };
        add(behave);

        createNetGrossLabels(table);

        table.add(new DataTableBehavior(DataTableBehavior.getRecommendedOptions()));
        table.add(new ListView<String>("headerRow", model(from(data.getHeadline()))) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("headerItem", item.getModelObject()));
            }
        });
        table.add(new ListView<ContractBaseData>("contractRows", data) {
            @Override
            protected void populateItem(ListItem<ContractBaseData> item) {
                long contractId = item.getModelObject().getContractId();
                double taxCoefficient = 1.0;

                if (BudgeteerSession.get().isTaxEnabled()) {
                    taxCoefficient = 1.0 + item.getModelObject().getTaxRate() / 100.0;
                }

                item.add(new HiddenField<>("contractID", Model.of(contractId)));

                BookmarkablePageLink<EditContractPage> link = new BookmarkablePageLink<>("editContract",
                        ContractDetailsPage.class, EditContractPage.createParameters(contractId));
                link.add(new Label("contractName", model(from(item.getModelObject()).getContractName())));
                item.add(link);
                item.add(new Label("internalNumber", model(from(item.getModelObject()).getInternalNumber())));
                item.add(new DateLabel("startDate", model(from(item.getModelObject()).getStartDate())));
                item.add(new EnumLabel<>("type", model(from(item.getModelObject()).getType())));
                item.add(new ListView<DynamicAttributeField>("contractRow", model(from(item.getModelObject()).getContractAttributes())) {
                    @Override
                    protected void populateItem(ListItem<DynamicAttributeField> item) {
                        item.add(new Label("contractRowText", item.getModelObject().getValue()));
                    }
                });
                item.add(new Label("budgetTotal", Model.of(MoneyUtil.toDouble(item.getModelObject().getBudget(),
                        BudgeteerSession.get().getSelectedBudgetUnit(), taxCoefficient))));
                item.add(new Label("budgetSpent", Model.of(MoneyUtil.toDouble(item.getModelObject().getBudgetSpent(),
                        BudgeteerSession.get().getSelectedBudgetUnit(), taxCoefficient))));
                item.add(new Label("budgetLeft", Model.of(MoneyUtil.toDouble(item.getModelObject().getBudgetLeft(),
                        BudgeteerSession.get().getSelectedBudgetUnit(), taxCoefficient))));
                item.add(new BookmarkablePageLink("editLink", EditContractPage.class,
                        EditContractPage.createParameters(contractId)));
            }
        });

        table.add(new ListView<String>("footerRow", model(from(data.getFooter()))) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("footerItem", item.getModelObject()));
            }
        });

        addTableSummaryLabels(table, data);
        add(table);

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                response.render(OnDomReadyHeaderItem.forScript(
                        "var table;\n" +
                                "var draggedRow;\n" +
                                "var handleBtn;\n" +
                                "var target = false;\n" +
                                "\n" +
                                "$(document).ready(function(){\n" +
                                "   table = $('.table').DataTable();\n" +
                                "   // Remove arrows in the header cell of the sorting buttons\n" +
                                "   $('#btnsHeadCell').removeClass('sorting_asc');\n" +
                                "\n"+
                                "   table.on('draw', function(){\n" +
                                "       $('.upBtn').on('click',function(event){\n" +
                                "           up(event);\n" +
                                "       });\n"+
                                "\n" +
                                "       $('.downBtn').on('click', function(event){\n" +
                                "           down(event);\n"+
                                "       });\n"+
                                "   })\n"+
                                "\n" +
                                "   // Get all row indices of the table\n" +
                                "   function getAllIndices(){\n" +
                                "       var indices = [];\n" +
                                "       table.rows().eq(0).each( function ( index ) {\n" +
                                "           indices.push(index);\n" +
                                "       });\n" +
                                "       return indices;\n" +
                                "   }\n" +
                                "\n" +
                                "   // Get the row index of the element which triggered the event\n" +
                                "   function getRowIndex(event){\n" +
                                "       var source = event.target || event.srcElement;\n" +
                                "       var thisRow = $(source).closest('tr');\n" +
                                "       return table.row(thisRow).index();\n" +
                                "   }\n" +
                                "\n" +
                                "   // Swap the rows of the two indices\n" +
                                "   function swapRows(firstIndex, secondIndex) {\n" +
                                "       var firstRowData = table.row(firstIndex).data();\n" +
                                "       var secondRowData = table.row(secondIndex).data();\n" +
                                "\n" +
                                "       if(typeof secondRowData !== 'undefined'){\n" +
                                "           table.row(firstIndex).data(secondRowData).draw(false);\n" +
                                "           table.row(secondIndex).data(firstRowData).draw(false);\n" +
                                "       }\n" +
                                "   }\n" +
                                "\n" +
                                "   function down(event){\n" +
                                "       var page = table.page();\n" +
                                "       // Order by the first column, else row swapping doesn't work\n" +
                                "       table.order([0, 'asc']);\n" +
                                "       var thisIndex = getRowIndex(event);\n" +
                                "       var allIndices = getAllIndices();\n" +
                                "       var arrayIndex = allIndices.indexOf(thisIndex);\n" +
                                "       var nextIndex = allIndices[arrayIndex+1];\n" +
                                "       swapRows(thisIndex, nextIndex);\n" +
                                "       table.page(page).draw(false );\n" +
                                "       saveSorting();\n" +
                                "   }\n"+
                                "\n" +
                                "   function up(event){\n" +
                                "       var page = table.page();\n" +
                                "       // Order by the first column, else row swapping doesn't work\n" +
                                "       table.order([0, 'asc']);\n" +
                                "       var thisIndex = getRowIndex(event);\n" +
                                "       var allIndices = getAllIndices();\n" +
                                "       var arrayIndex = allIndices.indexOf(thisIndex);\n" +
                                "       var previousIndex = allIndices[arrayIndex-1];\n" +
                                "       swapRows(thisIndex, previousIndex);\n" +
                                "       table.page(page).draw( false );\n" +
                                "       saveSorting();\n" +
                                "   }\n"+
                                "\n" +
                                "   $('tr').click(function(){\n" +
                                "       // Remove arrows in the header cell of the sorting buttons\n" +
                                "       $('#btnsHeadCell').removeClass('sorting');\n" +
                                "       saveSorting();\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.upBtn').on('mouseenter', function(event){\n" +
                                "       hover(event, true);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.upBtn').on('mouseleave', function(event){\n" +
                                "       hover(event, false);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.upBtn').on('click',function(event){\n" +
                                "       up(event);" +
                                "   });\n"+
                                "\n" +
                                "   $('.downBtn').on('click', function(event){\n" +
                                "       down(event);"+
                                "   });\n"+
                                "\n" +
                                "   $('.downBtn').on('mouseenter', function(event){\n" +
                                "       hover(event, true);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.downBtn').on('mouseleave', function(event){\n" +
                                "       hover(event, false);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.dragBtn').on('mouseenter', function(event){\n" +
                                "       hover(event, true);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.dragBtn').on('mouseleave', function(event){\n" +
                                "       hover(event, false);\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.sorting-row').on('mousedown',function(event){\n" +
                                "       target = event.target;\n" +
                                "       handleBtn = $(event).closest('.dragBtn');\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.sorting-row').on('drop', function(event){ \n" +
                                "       event.stopPropagation();\n" +
                                "       event.preventDefault();\n" +
                                "       var currentIndex = getRowIndex(event);\n" +
                                "       var oldIndex = table.row(draggedRow).index();\n" +
                                "       swapRows(currentIndex, oldIndex);\n" +
                                "       saveSorting();\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.sorting-row').on('dragover', function(ev){ \n" +
                                "       ev.preventDefault();\n" +
                                "   });\n" +
                                "\n" +
                                "   $('.sorting-row').on('dragstart', function(event){\n" +
                                "       event.dataTransfer = event.originalEvent.dataTransfer;\n"+
                                "       if($(target).hasClass('dragBtn') || $(target).hasClass('fa-hand-o-up')){\n" +
                                "           event.dataTransfer.setData('text/plain', 'handleBtn');\n" +
                                "           // Order by the first column, else row swapping doesn't work\n" +
                                "           table.order([0, 'asc']);\n" +
                                "           draggedRow = $(event.target);\n" +
                                "       }\n" +
                                "       else{\n" +
                                "           event.preventDefault();\n" +
                                "       }\n" +
                                "   });\n" +
                                "\n" +
                                "   function saveSorting(){\n" +
                                "       var rows = table.rows();\n" +
                                "       var contractIDs = [];\n" +
                                "\n" +
                                "       rows.iterator('row', function(context, index){\n" +
                                "           var node = $(this.row(index).node());\n" +
                                "           var contractID = $(node).find(\"input\").get(0);\n" +
                                "           contractIDs.push($(contractID).val());\n" +
                                "       });\n" +
                                "\n" +
                                "       postAJAX(contractIDs);\n" +
                                "\n" +
                                "       // Remove arrows in the header cell of the sorting buttons\n" +
                                "       $(\"#btnsHeadCell\").removeClass('sorting_asc sorting');\n" +
                                "   }\n" +
                                "\n" +
                                "   function postAJAX(tableData){\n" +
                                "       try{\n" +
                                "           var commandToSend = 'tableData ='+tableData;\n" +
                                "           var wcall = Wicket.Ajax.post({\n" +
                                "               u: callbackUrl + '&'+commandToSend\n" +
                                "           });\n" +
                                "       }\n" +
                                "       catch(e){\n" +
                                "       }\n" +
                                "       $('.upBtn').on('click',function(event){\n" +
                                "           up(event);" +
                                "       });\n"+
                                "\n" +
                                "       $('.downBtn').on('click', function(event){\n" +
                                "           down(event);"+
                                "       });\n"+
                                "   }\n" +
                                "});\n"+
                                "function hover(event, show){\n" +
                                "   var source = event.target || event.srcElement;\n" +
                                "   \n" +
                                "   if(show){\n" +
                                "       $(source).tooltip('show');\n" +
                                "   }\n" +
                                "   else{\n" +
                                "       $(source).tooltip('hide');\n" +
                                "   }\n" +
                                "}"
                                ));
            }
        });
    }

    private void addTableSummaryLabels(WebMarkupContainer table, IModel<List<ContractBaseData>> model) {

        IModel<ContractTotalData> totalModel = new TotalContractDetailsModel(model);

        // Fill up the columns which contain the contract attributes with empty cells
        RepeatingView repeatingView = new RepeatingView("contractAttributeCell");
        for (int i = 0; i < ((TotalContractDetailsModel) totalModel).getContractAttributeSize(); i++) {
            repeatingView.add(new Label(repeatingView.newChildId(), ""));
        }

        table.add(repeatingView);

        table.add(new MoneyLabel("totalAmount",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudget()))),
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudgetGross())))
                )));
        table.add(new MoneyLabel("totalSpent",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudgetSpent()))),
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudgetSpentGross())))
                )));
        table.add(new MoneyLabel("totalRemaining",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudgetLeft()))),
                        new BudgetUnitMoneyModel(model(from(totalModel.getObject().getBudgetLeftGross())))
                )));
    }

    private void createNetGrossLabels(WebMarkupContainer table) {
        table.add(new Label("totalLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.contract.label.total", this))));
        table.add(new Label("leftLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.contract.label.left", this))));
        table.add(new Label("spentLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.contract.label.spent", this))));
    }
}