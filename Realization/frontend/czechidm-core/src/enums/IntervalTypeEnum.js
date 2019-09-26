import AbstractEnum from './AbstractEnum';

/**
 * Interval type - minute, hour, day, week, month
 *
 * @author Petr Hanák
 */
export default class IntervalTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IntervalTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

IntervalTypeEnum.MINUTE = Symbol('MINUTE');
IntervalTypeEnum.HOUR = Symbol('HOUR');
IntervalTypeEnum.DAY = Symbol('DAY');
IntervalTypeEnum.WEEK = Symbol('WEEK');
IntervalTypeEnum.MONTH = Symbol('MONTH');
