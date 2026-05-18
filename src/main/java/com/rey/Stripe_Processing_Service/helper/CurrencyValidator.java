package com.rey.Stripe_Processing_Service.helper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "usd", "aed", "afn", "all", "amd", "ang", "aoa", "ars", "aud", "awg",
            "azn", "bam", "bbd", "bdt", "bgn", "bif", "bmd", "bnd", "bob", "brl",
            "bsd", "bwp", "byn", "bzd", "cad", "cdf", "chf", "clp", "cny", "cop",
            "crc", "cve", "czk", "djf", "dkk", "dop", "dzd", "egp", "etb", "eur",
            "fjd", "fkp", "gbp", "gel", "gip", "gmd", "gnf", "gtq", "gyd", "hkd",
            "hnl", "hrk", "htg", "huf", "idr", "ils", "inr", "isk", "jmd", "jpy",
            "kes", "kgs", "khr", "kmf", "krw", "kyd", "kzt", "lak", "lbp", "lkr",
            "lrd", "lsl", "mad", "mdl", "mga", "mkd", "mmk", "mnt", "mop", "mur",
            "mvr", "mwk", "mxn", "myr", "mzn", "nad", "ngn", "nio", "nok", "npr",
            "nzd", "pab", "pen", "pgk", "php", "pkr", "pln", "pyg", "qar", "ron",
            "rsd", "rub", "rwf", "sar", "sbd", "scr", "sek", "sgd", "shp", "sle",
            "sos", "srd", "std", "szl", "thb", "tjs", "top", "try", "ttd", "twd",
            "tzs", "uah", "ugx", "uyu", "uzs", "vnd", "vuv", "wst", "xaf", "xcd",
            "xcg", "xof", "xpf", "yer", "zar", "zmw"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return SUPPORTED_CURRENCIES.contains(value.toLowerCase());
    }
}
