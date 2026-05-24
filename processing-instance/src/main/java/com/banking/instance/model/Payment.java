package com.banking.instance.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String debtorIban;

	@Column(nullable = false)
	private String creditorIban;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	private String remittanceInfo;
	private LocalDateTime createdAt;

	public enum PaymentMethod {SEPA_CREDIT, SEPA_INSTANT, DOMESTIC_WIRE}

	public enum PaymentStatus {CREATED, EXECUTED, REJECTED}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		if (status == null) {
			status = PaymentStatus.CREATED;
		}
	}

	public Payment() {
	}

	public Payment(String debtorIban, String creditorIban, BigDecimal amount,
				   PaymentMethod method, String remittanceInfo) {
		this.debtorIban = debtorIban;
		this.creditorIban = creditorIban;
		this.amount = amount;
		this.method = method;
		this.remittanceInfo = remittanceInfo;
	}

	public String getId() {
		return id;
	}

	public String getDebtorIban() {
		return debtorIban;
	}

	public void setDebtorIban(String s) {
		this.debtorIban = s;
	}

	public String getCreditorIban() {
		return creditorIban;
	}

	public void setCreditorIban(String s) {
		this.creditorIban = s;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal a) {
		this.amount = a;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod m) {
		this.method = m;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus s) {
		this.status = s;
	}

	public String getRemittanceInfo() {
		return remittanceInfo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
