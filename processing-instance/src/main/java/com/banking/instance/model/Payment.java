package com.banking.instance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
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

	public Payment(String debtorIban, String creditorIban, BigDecimal amount,
				   PaymentMethod method, String remittanceInfo) {
		this.debtorIban = debtorIban;
		this.creditorIban = creditorIban;
		this.amount = amount;
		this.method = method;
		this.remittanceInfo = remittanceInfo;
	}
}
