package com.ksider.mobile.android.view.materialcalendarview;

import com.ksider.mobile.android.view.materialcalendarview.decorators.DayViewDecorator;

class DecoratorResult {
    public final DayViewDecorator decorator;
    public final DayViewFacade result;

    DecoratorResult(DayViewDecorator decorator, DayViewFacade result) {
        this.decorator = decorator;
        this.result = result;
    }
}
