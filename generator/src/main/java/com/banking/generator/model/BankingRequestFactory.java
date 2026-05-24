package com.banking.generator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BankingRequestFactory {

	private static final Random random = new Random();

	private static final List<String> IBANS = List.of(
			"RO49AAAA1B31007593840000",
			"RO49AAAA1B31007593840001",
			"RO49AAAA1B31007593840002",
			"RO49AAAA1B31007593840003",
			"RO49AAAA1B31007593840004"
	);

	private static final List<String> NAMES = List.of(
			"Ion Popescu", "Maria Ionescu", "SC Alfa SRL",
			"Andrei Dumitrescu", "Elena Constantin"
	);

	private static final List<String> DESCRIPTIONS = List.of(
			"Plata factura curent", "Achizitie echipamente", "Chirie lunara",
			"Salariu angajati", "Abonament servicii", "Rambursare credit",
			"Dividende", "Consultanta IT", "Furnizor materii prime"
	);

	public record Request(String method, String path, Object body) {
	}

	public static Request createAccount() {
		return new Request("POST", "/api/accounts", Map.of(
				"iban", "RO49TEST" + String.format("%016d", Math.abs(random.nextLong()) % 9_999_999_999_999_999L),
				"ownerName", NAMES.get(random.nextInt(NAMES.size())),
				"balance", randomAmount(100, 50000),
				"type", randomFrom("CURRENT", "SAVINGS", "BUSINESS")
		));
	}

	public static Request getAccounts() {
		return new Request("GET", "/api/accounts", null);
	}

	public static Request getAccountStats() {
		return new Request("GET", "/api/accounts/stats", null);
	}

	public static Request createTransaction() {
		String from = IBANS.get(random.nextInt(IBANS.size()));
		String to;
		do {
			to = IBANS.get(random.nextInt(IBANS.size()));
		} while (to.equals(from));
		return new Request("POST", "/api/transactions", Map.of(
				"fromIban", from,
				"toIban", to,
				"amount", randomAmount(50, 10000),
				"description", DESCRIPTIONS.get(random.nextInt(DESCRIPTIONS.size()))
		));
	}

	public static Request getTransactions() {
		return new Request("GET", "/api/transactions", null);
	}

	public static Request getPendingTxns() {
		return new Request("GET", "/api/transactions/pending", null);
	}

	public static Request getTransactionStats() {
		return new Request("GET", "/api/transactions/stats", null);
	}

	public static Request createPayment() {
		String debtor = IBANS.get(random.nextInt(IBANS.size()));
		String creditor;
		do {
			creditor = IBANS.get(random.nextInt(IBANS.size()));
		} while (creditor.equals(debtor));
		return new Request("POST", "/api/payments", Map.of(
				"debtorIban", debtor,
				"creditorIban", creditor,
				"amount", randomAmount(100, 25000),
				"method", randomFrom("SEPA_CREDIT", "SEPA_INSTANT", "DOMESTIC_WIRE"),
				"remittanceInfo", DESCRIPTIONS.get(random.nextInt(DESCRIPTIONS.size()))
		));
	}

	public static Request getPayments() {
		return new Request("GET", "/api/payments", null);
	}

	public static Request getPaymentStats() {
		return new Request("GET", "/api/payments/stats", null);
	}

	public static Request uniform() {
		return switch (random.nextInt(10)) {
			case 0, 1 -> createTransaction();
			case 2, 3 -> getTransactions();
			case 4, 5 -> createPayment();
			case 6 -> getPayments();
			case 7 -> createAccount();
			case 8 -> getAccounts();
			default -> getTransactionStats();
		};
	}

	public static Request burst() {
		return switch (random.nextInt(10)) {
			case 0, 1, 2 -> createTransaction();
			case 3, 4, 5 -> createPayment();
			case 6, 7 -> getPendingTxns();
			case 8 -> getPaymentStats();
			default -> getTransactionStats();
		};
	}

	public static Request gradual(int phase) {
		if (phase <= 3) {
			return random.nextBoolean() ? getAccounts() : getTransactions();
		} else if (phase <= 7) {
			return uniform();
		} else {
			return burst();
		}
	}

	private static BigDecimal randomAmount(double min, double max) {
		return BigDecimal.valueOf(min + random.nextDouble() * (max - min))
				.setScale(2, RoundingMode.HALF_UP);
	}

	@SafeVarargs
	private static <T> T randomFrom(T... values) {
		return values[random.nextInt(values.length)];
	}
}
