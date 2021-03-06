import AbstractEnum from './AbstractEnum';

/**
 * Trigger type - simple, repeat, cron, dependent
 *
 * @author Radek Tomiška
 */
export default class TriggerTypeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.TriggerTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.SIMPLE: {
        return 'success';
      }
      // change success to primary or danger?
      case this.REPEAT: {
        return 'success';
      }
      case this.CRON: {
        return 'info';
      }
      case this.DEPENDENT: {
        return 'warning';
      }
      default: {
        return null;
      }
    }
  }

  /**
   * Returns BE trigger type
   *
   * @param  {string} key
   * @return {string} java simple name
   */
  static getTriggerType(key) {
    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.SIMPLE: {
        return 'SimpleTaskTrigger';
      }
      case this.REPEAT: {
        return 'CronTaskTrigger';
      }
      case this.CRON: {
        return 'CronTaskTrigger';
      }
      case this.DEPENDENT: {
        return 'DependentTaskTrigger';
      }
      default: {
        throw Error(`Type [${key}] not implemented`);
      }
    }
  }
}

TriggerTypeEnum.SIMPLE = Symbol('SIMPLE');
TriggerTypeEnum.REPEAT = Symbol('REPEAT');
TriggerTypeEnum.CRON = Symbol('CRON');
TriggerTypeEnum.DEPENDENT = Symbol('DEPENDENT');
