package com.furbo.repository;

import com.furbo.config.TestcontainersConfig;
import com.furbo.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByBuyerIdOrSellerId_ReturnsTransactions() {
        Transaction t1 = new Transaction();
        t1.setBuyerId(1L);
        t1.setSellerId(2L);
        
        transactionRepository.save(t1);

        List<Transaction> found = transactionRepository.findByBuyerIdOrSellerId(1L, 3L);
        
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getBuyerId()).isEqualTo(1L);
    }
}
