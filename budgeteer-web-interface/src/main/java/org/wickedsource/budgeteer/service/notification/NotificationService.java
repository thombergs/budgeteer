package org.wickedsource.budgeteer.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.record.RecordRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MissingDailyRateNotificationMapper missingDailyRateMapper;

    @Autowired
    private MissingBudgetTotalNotificationMapper missingBudgetTotalNotificationMapper;

    /**
     * Returns all notifications currently available for the given project
     *
     * @param projectId ID of the project whose notifications to load
     * @return list of notifications
     */
    public List<Notification> getNotifications(long projectId) {
        List<Notification> notifications = new ArrayList<Notification>();
        notifications.addAll(missingDailyRateMapper.map(recordRepository.getMissingDailyRatesForProject(projectId)));
        notifications.addAll(missingBudgetTotalNotificationMapper.map(budgetRepository.getMissingBudgetTotalsForProject(projectId)));
        return notifications;
    }

    /**
     * Returns all notifications currently available concerning the given person.
     *
     * @param personId id of the person about whom notifications should be returned.
     * @return list of notifications concerning the given person.
     */
    public List<Notification> getNotificationsForPerson(long personId) {
        return Arrays.asList(missingDailyRateMapper.map(recordRepository.getMissingDailyRatesForPerson(personId)));
    }

    /**
     * Returns all notifications currently available concerning the given budget.
     *
     * @param budgetId id of the budget about which notifications should be returned.
     * @return list of notifications concerning the given budget.
     */
    public List<Notification> getNotificationsForBudget(long budgetId) {
        return Arrays.asList(missingBudgetTotalNotificationMapper.map(budgetRepository.getMissingBudgetTotalForBudget(budgetId)));
    }

}
