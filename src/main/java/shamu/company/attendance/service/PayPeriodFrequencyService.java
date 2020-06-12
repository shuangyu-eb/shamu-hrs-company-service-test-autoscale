package shamu.company.attendance.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;

@Service
public class PayPeriodFrequencyService {
    private final PayPeriodFrequencyRepository payPeriodFrequencyRepository;

    @Autowired
    public PayPeriodFrequencyService(PayPeriodFrequencyRepository payPeriodFrequencyRepository) {
        this.payPeriodFrequencyRepository = payPeriodFrequencyRepository;
    }

    public List<StaticCompanyPayFrequencyType> findAll() {
        return payPeriodFrequencyRepository.findAll();
    }
}
