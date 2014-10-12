package org.wickedsource.budgeteer.web.usecase.people.monthreport.component.monthreporttable;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.hours.AggregatedRecord;
import org.wickedsource.budgeteer.service.hours.AggregationService;

import java.util.List;

public class PersonMonthlyAggregatedRecordsModel extends LoadableDetachableModel<List<AggregatedRecord>> {

    @SpringBean
    private AggregationService service;

    private long personId;

    public PersonMonthlyAggregatedRecordsModel(long personId) {
        Injector.get().inject(this);
        this.personId = personId;
    }

    @Override
    protected List<AggregatedRecord> load() {
        return service.getMonthlyAggregationForPerson(personId);
    }
}