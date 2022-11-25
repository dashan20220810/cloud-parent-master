package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CodeNumChange;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.repository.CodeNumChangeRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeNumChangeService {
    @Autowired
    private CodeNumChangeRepository codeNumChangeRepository;

    public void save(Long userId, GameRecord record, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter) {
        CodeNumChange codeNumChange=new CodeNumChange();
        codeNumChange.setUserId(userId);
        if (record != null) {
            codeNumChange.setGameRecordId(record.getId());
            codeNumChange.setBetId(record.getBetId());
        }
        codeNumChange.setAmount(amount);
        codeNumChange.setAmountBefore(amountBefore);
        codeNumChange.setAmountAfter(amountAfter);
        codeNumChangeRepository.save(codeNumChange);
    }

    public void save(CodeNumChange codeNumChange){
        codeNumChangeRepository.save(codeNumChange);
    }

    public Page<CodeNumChange> findCodeNumChangePage(Pageable pageable, CodeNumChange codeNumChange){
        Specification<CodeNumChange> condition = this.getCondition(codeNumChange);
        Page<CodeNumChange> all = codeNumChangeRepository.findAll(condition, pageable);
        return all;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<CodeNumChange> getCondition(CodeNumChange codeNumChange) {
        Specification<CodeNumChange> specification = new Specification<CodeNumChange>() {
            @Override
            public Predicate toPredicate(Root<CodeNumChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (codeNumChange.getGameRecordId() !=null) {
                    list.add(cb.equal(root.get("gameRecordId").as(Long.class), codeNumChange.getGameRecordId()));
                }
                if (codeNumChange.getUserId() !=null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), codeNumChange.getUserId()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}
