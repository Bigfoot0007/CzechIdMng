import { Enums } from 'czechidm-core';

/**
 * Keys of Tree fields
 */
export default class TreeAttributeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.TreeAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`acc:enums.TreeAttributeEnum.helpBlock.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.CODE: {
        return 'code';
      }
      case this.NAME: {
        return 'name';
      }
      case this.PARENT: {
        return 'parent';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'code': {
        return this.CODE;
      }
      case 'name': {
        return this.NAME;
      }
      case 'parent': {
        return this.PARENT;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

TreeAttributeEnum.CODE = Symbol('CODE');
TreeAttributeEnum.NAME = Symbol('NAME');
TreeAttributeEnum.PARENT = Symbol('PARENT');
TreeAttributeEnum.DISABLED = Symbol('DISABLED');
