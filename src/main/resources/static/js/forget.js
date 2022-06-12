$(function (){
    $("#verifyCodeBtn").click(getVerifyCode);
});
function getVerifyCode(){
    var email = $("#your-email").val();
    if (!email){
        alert("请先填写邮箱");
        return false;
    }

        $.get(
            CONTEXT_PATH + "/forget/code",
            {"email":email},
            function (data){
                data = $.parseJSON(data);
                if (data.code == 0 ){
                    alert("验证码已经发送至邮箱，请登陆邮箱查看");
                }else{
                    alert(data.msg)
                }
            }
        );
}