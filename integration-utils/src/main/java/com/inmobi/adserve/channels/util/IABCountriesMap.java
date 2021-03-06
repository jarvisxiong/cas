package com.inmobi.adserve.channels.util;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;


public class IABCountriesMap {
    private static HashMap<String, String> countriesToIABMapping = new HashMap<String, String>();

    private IABCountriesMap() {}

    static {
        countriesToIABMapping.put("AF", "AFG");
        countriesToIABMapping.put("AX", "ALA");
        countriesToIABMapping.put("AL", "ALB");
        countriesToIABMapping.put("DZ", "DZA");
        countriesToIABMapping.put("AS", "ASM");
        countriesToIABMapping.put("AD", "AND");
        countriesToIABMapping.put("AO", "AGO");
        countriesToIABMapping.put("AI", "AIA");
        countriesToIABMapping.put("AQ", "ATA");
        countriesToIABMapping.put("AG", "ATG");
        countriesToIABMapping.put("AR", "ARG");
        countriesToIABMapping.put("AM", "ARM");
        countriesToIABMapping.put("AW", "ABW");
        countriesToIABMapping.put("AU", "AUS");
        countriesToIABMapping.put("AT", "AUT");
        countriesToIABMapping.put("AZ", "AZE");
        countriesToIABMapping.put("BS", "BHS");
        countriesToIABMapping.put("BH", "BHR");
        countriesToIABMapping.put("BD", "BGD");
        countriesToIABMapping.put("BB", "BRB");
        countriesToIABMapping.put("BY", "BLR");
        countriesToIABMapping.put("BE", "BEL");
        countriesToIABMapping.put("BZ", "BLZ");
        countriesToIABMapping.put("BJ", "BEN");
        countriesToIABMapping.put("BM", "BMU");
        countriesToIABMapping.put("BT", "BTN");
        countriesToIABMapping.put("BO", "BOL");
        countriesToIABMapping.put("BA", "BIH");
        countriesToIABMapping.put("BW", "BWA");
        countriesToIABMapping.put("BV", "BVT");
        countriesToIABMapping.put("BR", "BRA");
        countriesToIABMapping.put("IO", "IOT");
        countriesToIABMapping.put("BN", "BRN");
        countriesToIABMapping.put("BG", "BGR");
        countriesToIABMapping.put("BF", "BFA");
        countriesToIABMapping.put("MM", "MMR");
        countriesToIABMapping.put("BI", "BDI");
        countriesToIABMapping.put("KH", "KHM");
        countriesToIABMapping.put("CM", "CMR");
        countriesToIABMapping.put("CA", "CAN");
        countriesToIABMapping.put("CV", "CPV");
        countriesToIABMapping.put("BQ", "BES");
        countriesToIABMapping.put("KY", "CYM");
        countriesToIABMapping.put("CF", "CAF");
        countriesToIABMapping.put("TD", "TCD");
        countriesToIABMapping.put("CL", "CHL");
        countriesToIABMapping.put("CN", "CHN");
        countriesToIABMapping.put("CX", "CXR");
        countriesToIABMapping.put("CO", "COL");
        countriesToIABMapping.put("KM", "COM");
        countriesToIABMapping.put("CG", "COG");
        countriesToIABMapping.put("CD", "COD");
        countriesToIABMapping.put("CK", "COK");
        countriesToIABMapping.put("CR", "CRI");
        countriesToIABMapping.put("CI", "CIV");
        countriesToIABMapping.put("HR", "HRV");
        countriesToIABMapping.put("CU", "CUB");
        countriesToIABMapping.put("CW", "CUW");
        countriesToIABMapping.put("CY", "CYP");
        countriesToIABMapping.put("CZ", "CZE");
        countriesToIABMapping.put("DK", "DNK");
        countriesToIABMapping.put("DJ", "DJI");
        countriesToIABMapping.put("DM", "DMA");
        countriesToIABMapping.put("DO", "DOM");
        countriesToIABMapping.put("TL", "TLS");
        countriesToIABMapping.put("EC", "ECU");
        countriesToIABMapping.put("EG", "EGY");
        countriesToIABMapping.put("SV", "SLV");
        countriesToIABMapping.put("GQ", "GNQ");
        countriesToIABMapping.put("ER", "ERI");
        countriesToIABMapping.put("EE", "EST");
        countriesToIABMapping.put("ET", "ETH");
        countriesToIABMapping.put("FK", "FLK");
        countriesToIABMapping.put("FO", "FRO");
        countriesToIABMapping.put("FJ", "FJI");
        countriesToIABMapping.put("FI", "FIN");
        countriesToIABMapping.put("FR", "FRA");
        countriesToIABMapping.put("GF", "GUF");
        countriesToIABMapping.put("PF", "PYF");
        countriesToIABMapping.put("TF", "ATF");
        countriesToIABMapping.put("GA", "GAB");
        countriesToIABMapping.put("GM", "GMB");
        countriesToIABMapping.put("GE", "GEO");
        countriesToIABMapping.put("DE", "DEU");
        countriesToIABMapping.put("GH", "GHA");
        countriesToIABMapping.put("GI", "GIB");
        countriesToIABMapping.put("GR", "GRC");
        countriesToIABMapping.put("GL", "GRL");
        countriesToIABMapping.put("GD", "GRD");
        countriesToIABMapping.put("GP", "GLP");
        countriesToIABMapping.put("GU", "GUM");
        countriesToIABMapping.put("GT", "GTM");
        countriesToIABMapping.put("GG", "GGY");
        countriesToIABMapping.put("GN", "GIN");
        countriesToIABMapping.put("GW", "GNB");
        countriesToIABMapping.put("GY", "GUY");
        countriesToIABMapping.put("HT", "HTI");
        countriesToIABMapping.put("HM", "HMD");
        countriesToIABMapping.put("HN", "HND");
        countriesToIABMapping.put("HK", "HKG");
        countriesToIABMapping.put("HU", "HUN");
        countriesToIABMapping.put("IS", "ISL");
        countriesToIABMapping.put("IN", "IND");
        countriesToIABMapping.put("ID", "IDN");
        countriesToIABMapping.put("IR", "IRN");
        countriesToIABMapping.put("IQ", "IRQ");
        countriesToIABMapping.put("IE", "IRL");
        countriesToIABMapping.put("IM", "IMN");
        countriesToIABMapping.put("IL", "ISR");
        countriesToIABMapping.put("IT", "ITA");
        countriesToIABMapping.put("JM", "JAM");
        countriesToIABMapping.put("JP", "JPN");
        countriesToIABMapping.put("JE", "JEY");
        countriesToIABMapping.put("JO", "JOR");
        countriesToIABMapping.put("KZ", "KAZ");
        countriesToIABMapping.put("KE", "KEN");
        countriesToIABMapping.put("KI", "KIR");
        countriesToIABMapping.put("KW", "KWT");
        countriesToIABMapping.put("KG", "KGZ");
        countriesToIABMapping.put("LA", "LAO");
        countriesToIABMapping.put("LV", "LVA");
        countriesToIABMapping.put("LB", "LBN");
        countriesToIABMapping.put("LS", "LSO");
        countriesToIABMapping.put("LR", "LBR");
        countriesToIABMapping.put("LY", "LBY");
        countriesToIABMapping.put("LI", "LIE");
        countriesToIABMapping.put("LT", "LTU");
        countriesToIABMapping.put("LU", "LUX");
        countriesToIABMapping.put("MO", "MAC");
        countriesToIABMapping.put("MK", "MKD");
        countriesToIABMapping.put("MG", "MDG");
        countriesToIABMapping.put("MW", "MWI");
        countriesToIABMapping.put("MY", "MYS");
        countriesToIABMapping.put("MV", "MDV");
        countriesToIABMapping.put("ML", "MLI");
        countriesToIABMapping.put("MT", "MLT");
        countriesToIABMapping.put("MH", "MHL");
        countriesToIABMapping.put("MQ", "MTQ");
        countriesToIABMapping.put("MR", "MRT");
        countriesToIABMapping.put("MU", "MUS");
        countriesToIABMapping.put("YT", "MYT");
        countriesToIABMapping.put("MX", "MEX");
        countriesToIABMapping.put("FM", "FSM");
        countriesToIABMapping.put("MC", "MCO");
        countriesToIABMapping.put("MN", "MNG");
        countriesToIABMapping.put("ME", "MNE");
        countriesToIABMapping.put("MS", "MSR");
        countriesToIABMapping.put("MA", "MAR");
        countriesToIABMapping.put("MZ", "MOZ");
        countriesToIABMapping.put("NA", "NAM");
        countriesToIABMapping.put("NR", "NRU");
        countriesToIABMapping.put("NP", "NPL");
        countriesToIABMapping.put("NL", "NLD");
        countriesToIABMapping.put("AN", "ANT");
        countriesToIABMapping.put("NC", "NCL");
        countriesToIABMapping.put("NZ", "NZL");
        countriesToIABMapping.put("NI", "NIC");
        countriesToIABMapping.put("NE", "NER");
        countriesToIABMapping.put("NG", "NGA");
        countriesToIABMapping.put("NU", "NIU");
        countriesToIABMapping.put("NF", "NFK");
        countriesToIABMapping.put("KP", "PRK");
        countriesToIABMapping.put("MP", "MNP");
        countriesToIABMapping.put("NO", "NOR");
        countriesToIABMapping.put("PS", "PSE");
        countriesToIABMapping.put("OM", "OMN");
        countriesToIABMapping.put("PK", "PAK");
        countriesToIABMapping.put("PW", "PLW");
        countriesToIABMapping.put("PA", "PAN");
        countriesToIABMapping.put("PG", "PNG");
        countriesToIABMapping.put("PY", "PRY");
        countriesToIABMapping.put("PE", "PER");
        countriesToIABMapping.put("PH", "PHL");
        countriesToIABMapping.put("PN", "PCN");
        countriesToIABMapping.put("PL", "POL");
        countriesToIABMapping.put("PT", "PRT");
        countriesToIABMapping.put("PR", "PRI");
        countriesToIABMapping.put("QA", "QAT");
        countriesToIABMapping.put("MD", "MDA");
        countriesToIABMapping.put("RE", "REU");
        countriesToIABMapping.put("RO", "ROU");
        countriesToIABMapping.put("RU", "RUS");
        countriesToIABMapping.put("RW", "RWA");
        countriesToIABMapping.put("SH", "SHN");
        countriesToIABMapping.put("KN", "KNA");
        countriesToIABMapping.put("LC", "LCA");
        countriesToIABMapping.put("MF", "MAF");
        countriesToIABMapping.put("PM", "SPM");
        countriesToIABMapping.put("VC", "VCT");
        countriesToIABMapping.put("WS", "WSM");
        countriesToIABMapping.put("SM", "SMR");
        countriesToIABMapping.put("ST", "STP");
        countriesToIABMapping.put("SA", "SAU");
        countriesToIABMapping.put("SN", "SEN");
        countriesToIABMapping.put("RS", "SRB");
        countriesToIABMapping.put("RM", "SCG");
        countriesToIABMapping.put("SC", "SYC");
        countriesToIABMapping.put("SL", "SLE");
        countriesToIABMapping.put("SG", "SGP");
        countriesToIABMapping.put("SX", "SXM");
        countriesToIABMapping.put("SK", "SVK");
        countriesToIABMapping.put("SI", "SVN");
        countriesToIABMapping.put("SB", "SLB");
        countriesToIABMapping.put("SO", "SOM");
        countriesToIABMapping.put("ZA", "ZAF");
        countriesToIABMapping.put("GS", "SGS");
        countriesToIABMapping.put("KR", "KOR");
        countriesToIABMapping.put("SS", "SSD");
        countriesToIABMapping.put("ES", "ESP");
        countriesToIABMapping.put("LK", "LKA");
        countriesToIABMapping.put("SD", "SDN");
        countriesToIABMapping.put("SR", "SUR");
        countriesToIABMapping.put("SJ", "SJM");
        countriesToIABMapping.put("SZ", "SWZ");
        countriesToIABMapping.put("SE", "SWE");
        countriesToIABMapping.put("CH", "CHE");
        countriesToIABMapping.put("SY", "SYR");
        countriesToIABMapping.put("TW", "TWN");
        countriesToIABMapping.put("TJ", "TJK");
        countriesToIABMapping.put("TZ", "TZA");
        countriesToIABMapping.put("TH", "THA");
        countriesToIABMapping.put("TG", "TGO");
        countriesToIABMapping.put("TK", "TKL");
        countriesToIABMapping.put("TO", "TON");
        countriesToIABMapping.put("TT", "TTO");
        countriesToIABMapping.put("TN", "TUN");
        countriesToIABMapping.put("TR", "TUR");
        countriesToIABMapping.put("TM", "TKM");
        countriesToIABMapping.put("TC", "TCA");
        countriesToIABMapping.put("TV", "TUV");
        countriesToIABMapping.put("AE", "ARE");
        countriesToIABMapping.put("UG", "UGA");
        countriesToIABMapping.put("UA", "UKR");
        countriesToIABMapping.put("GB", "GBR");
        countriesToIABMapping.put("UM", "UMI");
        countriesToIABMapping.put("UY", "URY");
        countriesToIABMapping.put("US", "USA");
        countriesToIABMapping.put("UZ", "UZB");
        countriesToIABMapping.put("VU", "VUT");
        countriesToIABMapping.put("VA", "VAT");
        countriesToIABMapping.put("VE", "VEN");
        countriesToIABMapping.put("VN", "VNM");
        countriesToIABMapping.put("VG", "VGB");
        countriesToIABMapping.put("VI", "VIR");
        countriesToIABMapping.put("WF", "WLF");
        countriesToIABMapping.put("EH", "ESH");
        countriesToIABMapping.put("YE", "YEM");
        countriesToIABMapping.put("ZM", "ZMB");
        countriesToIABMapping.put("ZW", "ZWE");
    }

    /**
     * 
     * @param country
     * @return
     */
    public static String getIabCountry(final String country) {
        if (null == country) {
            return null;
        }
        final String iabCountry = countriesToIABMapping.get(country.toUpperCase());
        return StringUtils.isEmpty(iabCountry) ? countriesToIABMapping.get(country.toUpperCase()) : iabCountry;
    }

}
