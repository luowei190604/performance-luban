function alertMsg(msg) {
    alert(msg);
}


function communityView() {
    layer.open({
        type:2,
        area:  ['800px', '600px'],
        content: 'community.html',
        anim:1,
        closeBtn:2,
        cancel:function (index,layero) {
            console.info(index);
            console.info(layero);
        }
    });
}