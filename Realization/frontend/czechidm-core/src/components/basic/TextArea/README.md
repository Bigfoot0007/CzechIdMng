# TextArea component

Input component for long text. Extended from AbstractFormComponent.

## Parameters
All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| placeholder  | string   | Short description for input.|  |
| rows  | number   | Number of rows in text area  | 3 |
| min  | number   | Minimal number string characters for intput |  |
| max  | number   | Maximal number string characters for input  |  |
| warnIfTrimmable | bool | Enables/disables warning that there are any leading/trailing white-spaces in the input. | false |
## Usage

```html
<TextArea
   ref="description"
   label="Popis"
   placeholder="Poznámka k uživateli"
   rows={4}
   validation={Joi.string().length()}
   min={2}
   max={100}
   warnIfTrimmable
 />
```
