package com.banking.instance.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
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

	public BankAccount() {
	}

	public BankAccount(String iban, String ownerName, BigDecimal balance, AccountType type) {
		this.iban = iban;
		this.ownerName = ownerName;
		this.balance = balance;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String n) {
		this.ownerName = n;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
