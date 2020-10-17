package shamu.company.benefit.repository;

import shamu.company.benefit.entity.RetirementPayment;
import shamu.company.common.repository.BaseRepository;

public interface RetirementPaymentRepository extends BaseRepository<RetirementPayment, String> {

  @Override
  RetirementPayment save(RetirementPayment retirementPayment);

}
