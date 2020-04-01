1、指令级闭环实现

- 向topic发送指令
```
{
    "msgtype":"control",
    "signature:"1234",
    "actiontype":"send"
    "detail":{}
}
```
- 设备回复指令
```
{
    "msgtype":"control",
    "signature:"1234",
    "actiontype":"ack"
    "detail":{}
}
```
- 云平台确认设备回复
```
{
    "msgtype":"control",
    "signature:"1234",
    "actiontype":"ack_confirm"
    "detail":{}
}
```
- 设备结果确认
```
{
    "msgtype":"control",
    "signature:"1234",
    "actiontype":"complete/fail"
    "detail":{}
}
```