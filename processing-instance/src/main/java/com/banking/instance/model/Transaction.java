package com.banking.instance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String fromIban;

	@Column(nullable = false)
	private String toIban;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	private String description;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	private LocalDateTime createdAt;

	public enum TransactionStatus {PENDING, COMPLETED, FAILED}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		if (status == null) {
			status = TransactionStatus.PENDING;
		}
	}

	public Transaction(String fromIban, String toIban, BigDecimal amount, String description) {
		this.fromIban = fromIban;
		this.toIban = toIban;
		this.amount = amount;
		this.description = description;
	}
}
