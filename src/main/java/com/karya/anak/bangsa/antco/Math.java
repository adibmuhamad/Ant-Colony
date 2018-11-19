package com.karya.anak.bangsa.antco;

class Math {
    static double pow(final double base, final double exponent) {
        final long tmp = Double.doubleToLongBits(base);
        final long tmp2 = (long) (exponent * (tmp - 4606921280493453312L)) + 4606921280493453312L;
        return Double.longBitsToDouble(tmp2);
    }
}
