# DateValue component

Basic DateValue date formatter. Extended from AbstractComponent. You can use advanced component DateValue with default date/time format loaded from localization.

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| rendered | boolean | If component is rendered on page | true |
| value  | string | Date value in iso-8601 format |  |
| format  | string | Format for date time |  | |

## Usage

```html
<Basic.DateValue
  value="2016-05-02T00:00:00"
  format="d MMM"
/>
```
