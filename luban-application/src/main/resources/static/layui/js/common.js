/**
 * 数据回显公共方法
 *
 * @param parentDom 父元素
 * @param fieldName 需要回显的字段名称
 * @param data 一行数据
 * @param isEdit 是否可修改
 */
function echoVal(parentDom,fieldName,data,isEdit) {
    let inputValSelector = "input[name=" + fieldName + "]";
    let selectDomSelector = "select[name=" + fieldName + "]";
    parentDom.find(inputValSelector).val(data[fieldName]);
    parentDom.find(selectDomSelector).val(data[fieldName]);
    if (!isEdit) {
        parentDom.find(inputValSelector).attr('disabled','true');
        parentDom.find(selectDomSelector).attr('disabled','true');
    }
}