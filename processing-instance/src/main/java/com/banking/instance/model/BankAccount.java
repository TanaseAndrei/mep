package com.banking.instance.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, unique = true)
	private String iban;

	@Column(nullable = false)
	private String ownerName;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	private AccountType type;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	private LocalDateTime createdAt;

	public enum AccountType {CURRENT, SAVINGS, BUSINESS}

	public enum AccountStatus {ACTIVE, FROZEN, CLOSED}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		if (status == null) {
			status = AccountStatus.ACTIVE;
		}
	}

	public BankAccount(String iban, String ownerName, BigDecimal balance, AccountType type) {
		this.iban = iban;
		this.ownerName = ownerName;
		this.balance = balance;
		this.type = type;
	}
}
