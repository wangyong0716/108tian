package com.ksider.mobile.android.view.materialcalendarview.decorators;

import com.ksider.mobile.android.view.materialcalendarview.CalendarDay;
import com.ksider.mobile.android.view.materialcalendarview.DayViewFacade;
import com.ksider.mobile.android.view.materialcalendarview.DayViewSelectable;
import com.ksider.mobile.android.view.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

/**
 * Decorate several days with a dot
 */
public class EventDecorator implements DayViewDecorator,DayViewSelectable {

    private int color;
    private HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<CalendarDay>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color));
    }

    @Override
    public boolean selectable(CalendarDay day) {
        return  dates.contains(day);
    }
}
