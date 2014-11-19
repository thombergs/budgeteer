package org.wickedsource.budgeteer.service.record;

import com.mysema.query.types.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.imports.ImportEntity;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean;
import org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:spring-service.xml", "classpath:spring-repository-mock.xml"})
public class RecordServiceTest extends ServiceTestTemplate {

    @Autowired
    @ReplaceWithMock
    private RecordJoiner recordJoiner;

    @Autowired
    @ReplaceWithMock
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private RecordService service;

    @Test
    public void testGetWeeklyAggregationForPerson() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForPerson(1l);
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetMonthlyAggregationForPerson() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(anyListOf(MonthlyAggregatedRecordBean.class), anyListOf(MonthlyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getMonthlyAggregationForPerson(1l);
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetWeeklyAggregationForBudget() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1l);
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetMonthlyAggregationForBudget() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1l);
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetWeeklyAggregationForBudgets() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudgets(new BudgetTagFilter(Collections.EMPTY_LIST, 1l));
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetMonthlyAggregationForBudgets() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(anyListOf(MonthlyAggregatedRecordBean.class), anyListOf(MonthlyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getMonthlyAggregationForBudgets(new BudgetTagFilter(Collections.EMPTY_LIST, 1l));
        Assert.assertEquals(recordList, resultList);
    }

    @Test
    public void testGetFilteredRecords() throws Exception {
        List<WorkRecordEntity> recordList = createRecordList();
        when(workRecordRepository.findAll(any(Predicate.class))).thenReturn(recordList);
        List<WorkRecord> filteredRecords = service.getFilteredRecords(new WorkRecordFilter(1l));
        Assert.assertEquals(recordList.size(), filteredRecords.size());
        Assert.assertEquals(WorkRecord.class, filteredRecords.get(0).getClass());
    }

    private List<WorkRecordEntity> createRecordList() {
        List<WorkRecordEntity> list = new ArrayList<WorkRecordEntity>();
        WorkRecordEntity record = new WorkRecordEntity();
        record.setDailyRate(MoneyUtil.createMoney(100d));
        record.setDate(new Date());
        record.setId(1l);
        record.setBudget(new BudgetEntity());
        record.setImportRecord(new ImportEntity());
        record.setMinutes(480);
        record.setPerson(new PersonEntity());
        list.add(record);
        return list;
    }


    private List<AggregatedRecord> createAggregatedRecordList() {
        List<AggregatedRecord> list = new ArrayList<AggregatedRecord>();
        list.add(new AggregatedRecord());
        list.add(new AggregatedRecord());
        list.add(new AggregatedRecord());
        return list;
    }
}
