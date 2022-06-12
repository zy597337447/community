function like(btn,entityType,entityId,entityUserId){
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.entityLikeCount);
                $(btn).children("b").text(data.entityLikeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg)
            }
        }
    );
}