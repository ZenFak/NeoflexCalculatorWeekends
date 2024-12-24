package com.example.NeoflexCalculator.controllers;

import com.example.NeoflexCalculator.models.Day;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная страница");
        return "index";
    }

    @GetMapping("/calculate")
    public String findSalary(@RequestParam String salary,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateStart,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                             Model model) {

        //Общее кол-во выбранных дней
        long totalDays = ChronoUnit.DAYS.between(dateStart.toInstant(), dateEnd.toInstant()) + 1;
        double sal;     //Средняя зарплата за месяц

        try {
            sal = Double.parseDouble(salary);
        }catch (Exception e){
            return "error-data-type";
        }

        List<Day> days;
        days = parseCalendar();

        List<Day> weekendDays = getWeekendDays(dateStart, dateEnd, days);

        for (Day day : weekendDays) {
            if (day.workday() == 0) totalDays--;
        }

        double result = (sal * 12 / 365.0) * totalDays;

        model.addAttribute("result", round(result, 2));
        return "result";
    }

    /**
     * Заполняет List дней с указанием даты и статуса соответствующего дня (рабочий/выходной)
     * @return сформированный список дней
     */
    public List<Day> parseCalendar() {
        List<Day> days = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse("src/main/resources/static/xml/calendar2024.xml");
            doc.getDocumentElement().normalize();

            NodeList zap = doc.getElementsByTagName("ZAP");
            for (int temp = 0; temp < zap.getLength(); temp++){
                Element zapElement = (Element) zap.item(temp);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(zapElement.getElementsByTagName("DATE").item(0).getTextContent());

                int workday = Integer.parseInt(zapElement.getElementsByTagName("WORKDAY").item(0).getTextContent());

                days.add(new Day(date, workday));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return days;
    }

    /**
     * @param dateStart первый день отпуска
     * @param dateEnd последний день отпуска
     * @param days список дней класса Day
     * @return List weekendDays, хранящий только выбранные пользователем дни отпуска
     */
    public List<Day> getWeekendDays(Date dateStart, Date dateEnd, List<Day> days) {
        List<Day> weekendDays = new ArrayList<>();
        for (Day day : days) {
            if (day.date().compareTo(dateStart) >= 0 && day.date().compareTo(dateEnd) <= 0) {
                weekendDays.add(day);
            }
        }
        return weekendDays;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}