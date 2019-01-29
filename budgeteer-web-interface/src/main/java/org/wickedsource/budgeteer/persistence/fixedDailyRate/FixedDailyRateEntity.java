package org.wickedsource.budgeteer.persistence.fixedDailyRate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.Money;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "FIXED_DAILY_RATE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedDailyRateEntity implements Serializable {
    @Id
    @SequenceGenerator(name = "SEQ_FIXED_DAILY_RATE_ID", sequenceName = "SEQ_FIXED_DAILY_RATE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FIXED_DAILY_RATE_ID")
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BUDGET_ID")
    private BudgetEntity budget;

    @Temporal(TemporalType.DATE)
    @Column(name = "START_DATE")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "DAYS")
    private Integer days;

    @Column(name = "MONEY_AMOUNT", nullable = false)
    private Money moneyAmount;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedDailyRateEntity)) return false;
        FixedDailyRateEntity that = (FixedDailyRateEntity) o;
        if (id != that.id) return false;
        if (budget != null ? !budget.equals(that.budget) : that.budget != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (days != that.days) return false;
        if (moneyAmount != null ? !moneyAmount.equals(that.moneyAmount) : that.moneyAmount != null) return false;
        return true;
    }


    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (budget != null ? budget.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31* result + (days ^(days >>>32));
        result = 31 * result + (moneyAmount != null ? moneyAmount.hashCode() : 0);
        return result;
    }
}
