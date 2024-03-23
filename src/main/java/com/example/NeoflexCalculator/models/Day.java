package com.example.NeoflexCalculator.models;

import java.util.Date;

/**
 *
 * @param date дата в формате "yyyy-MM-dd"
 * @param workday статус дня (рабочий=1/выходной=0)
 */
public record Day(Date date, int workday) {
}
