package com.banking.instance.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
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

	public Transaction() {
	}

	public Transaction(String fromIban, String toIban, BigDecimal amount, String description) {
		this.fromIban = fromIban;
		this.toIban = toIban;
		this.amount = amount;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getFromIban() {
		return fromIban;
	}

	public void setFromIban(String s) {
		this.fromIban = s;
	}

	public String getToIban() {
		return toIban;
	}

	public void setToIban(String s) {
		this.toIban = s;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal a) {
		this.amount = a;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String d) {
		this.description = d;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus s) {
		this.status = s;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
