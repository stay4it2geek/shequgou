package com.act.quzhibo.common;


import android.app.Application;
import android.os.Environment;

import com.act.quzhibo.R;
import com.mabeijianxi.smallvideorecord2.DeviceUtils;
import com.mabeijianxi.smallvideorecord2.JianXiCamera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;


import cn.bmob.newim.BmobIM;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;

public class MyApplicaition extends Application {

    public static final LinkedHashMap<String, Integer> emotionsKeySrc = new LinkedHashMap();
    public static final LinkedHashMap<String, String> proKeySrc = new LinkedHashMap();
    public static final LinkedHashMap<String, String> cityKeySrc = new LinkedHashMap();
    public static  MyMessageHandler handler;

    public void onCreate() {
        super.onCreate();
        setInstance(this);
        handler= new MyMessageHandler(this);
        //初始化IM SDK，并注册消息接收器，只有主进程运行的时候才需要初始化
        if (getApplicationInfo().packageName.equals(getMyProcessName())) {
            BmobIM.init(this);
            BmobIM.registerDefaultMessageHandler(handler);
        }

        BmobConfig config = new BmobConfig.Builder(this)
                .setApplicationId("227399ddef86ccfa859443473306c43a")
                .setConnectTimeout(20)
                .setUploadBlockSize(1024 * 1024)
                .setFileExpiration(2500)
                .build();
        Bmob.initialize(config);
        // 设置拍摄视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/qushivideo/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/","/sdcard-ext/")+ "/qushivideo/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/qushivideo/");
        }
        JianXiCamera.initialize(false, null);
    }


    static {
        proKeySrc.put("1", "安徽省");
        proKeySrc.put("2", "福建省");
        proKeySrc.put("3", "甘肃省");
        proKeySrc.put("4", "广东省");
        proKeySrc.put("5", "贵州省");
        proKeySrc.put("6", "海南省");
        proKeySrc.put("7", "河北省");
        proKeySrc.put("8", "河南省");
        proKeySrc.put("9", "黑龙江省");
        proKeySrc.put("10", "湖北省");
        proKeySrc.put("11", "湖南省");
        proKeySrc.put("12", "吉林省");
        proKeySrc.put("13", "江苏省");
        proKeySrc.put("14", "江西省");
        proKeySrc.put("15", "辽宁省");
        proKeySrc.put("16", "青海省");
        proKeySrc.put("17", "山东省");
        proKeySrc.put("18", "山西省");
        proKeySrc.put("19", "陕西省");
        proKeySrc.put("20", "四川省");
        proKeySrc.put("21", "云南省");
        proKeySrc.put("22", "浙江省");

        proKeySrc.put("60", "广西壮族自治区");
        proKeySrc.put("61", "内蒙古自治区");
        proKeySrc.put("62", "宁夏回族自治区");
        proKeySrc.put("63", "西藏自治区");
        proKeySrc.put("64", "新疆维吾尔自治区");

        proKeySrc.put("83", "海外");
        proKeySrc.put("82", "澳门特别行政区");
        proKeySrc.put("81", "香港特别行政区");
        proKeySrc.put("51", "上海市");
        proKeySrc.put("53", "重庆市");
        proKeySrc.put("52", "天津市");
        proKeySrc.put("50", "北京市");
        proKeySrc.put("80", "台湾省");
    }

    static {
        emotionsKeySrc.put(":joy:", R.drawable.joy);
        emotionsKeySrc.put(":heart_eyes:", R.drawable.heart_eyes);
        emotionsKeySrc.put(":kissing_heart:", R.drawable.kissing_heart);
        emotionsKeySrc.put(":kissing_smiling_eyes:", R.drawable.kissing_smiling_eyes);
        emotionsKeySrc.put(":laughing:", R.drawable.laughing);
        emotionsKeySrc.put(":lollipop:", R.drawable.lollipop);
        emotionsKeySrc.put(":mask:", R.drawable.mask);
        emotionsKeySrc.put(":muscle:", R.drawable.muscle);
        emotionsKeySrc.put(":ok_hand:", R.drawable.ok_hand);
        emotionsKeySrc.put(":pensive:", R.drawable.pensive);
        emotionsKeySrc.put(":persevere:", R.drawable.persevere);
        emotionsKeySrc.put(":pig:", R.drawable.pig);
        emotionsKeySrc.put(":point_up:", R.drawable.point_up);
        emotionsKeySrc.put(":police_car:", R.drawable.police_car);
        emotionsKeySrc.put(":pray:", R.drawable.pray);
        emotionsKeySrc.put(":punch:", R.drawable.punch);
        emotionsKeySrc.put(":racehorse:", R.drawable.racehorse);
        emotionsKeySrc.put(":rage:", R.drawable.rage);
        emotionsKeySrc.put(":ramen:", R.drawable.ramen);
        emotionsKeySrc.put(":ring:", R.drawable.ring);
        emotionsKeySrc.put(":rose:", R.drawable.rose);
        emotionsKeySrc.put(":say_hello:", R.drawable.say_hello);
        emotionsKeySrc.put(":scream:", R.drawable.scream);
        emotionsKeySrc.put(":sleeping:", R.drawable.sleeping);
        emotionsKeySrc.put(":sleepy:", R.drawable.sleepy);
        emotionsKeySrc.put(":smile:", R.drawable.smile);
        emotionsKeySrc.put(":smiley:", R.drawable.smiley);
        emotionsKeySrc.put(":smiling_imp:", R.drawable.smiling_imp);
        emotionsKeySrc.put(":smirk:", R.drawable.smirk);
        emotionsKeySrc.put(":snake:", R.drawable.snake);
        emotionsKeySrc.put(":sob:", R.drawable.sob);
        emotionsKeySrc.put(":snake:", R.drawable.snake);
        emotionsKeySrc.put(":strawberry:", R.drawable.strawberry);
        emotionsKeySrc.put(":stuck_out_tongue:", R.drawable.stuck_out_tongue);
        emotionsKeySrc.put(":stuck_out_tongue_winking_eye:", R.drawable.stuck_out_tongue_winking_eye);
        emotionsKeySrc.put(":stuck_out_tongue_closed_eyes:", R.drawable.stuck_out_tongue_closed_eyes);
        emotionsKeySrc.put(":sunglasses:", R.drawable.sunglasses);
        emotionsKeySrc.put(":sunny:", R.drawable.sunny);
        emotionsKeySrc.put(":sweat:", R.drawable.sweat);
        emotionsKeySrc.put(":sweat_smile:", R.drawable.sweat_smile);
        emotionsKeySrc.put(":thumbsdown:", R.drawable.thumbsdown);
        emotionsKeySrc.put(":thumbsup:", R.drawable.thumbsup);
        emotionsKeySrc.put(":triumph:", R.drawable.triumph);
        emotionsKeySrc.put(":trophy:", R.drawable.trophy);
        emotionsKeySrc.put(":triumph:", R.drawable.triumph);
        emotionsKeySrc.put(":umbrella:", R.drawable.umbrella);
        emotionsKeySrc.put(":unamused:", R.drawable.unamused);
        emotionsKeySrc.put(":v:", R.drawable.v);
        emotionsKeySrc.put(":watermelon:", R.drawable.watermelon);
        emotionsKeySrc.put(":weary:", R.drawable.weary);
        emotionsKeySrc.put(":wink:", R.drawable.wink);
        emotionsKeySrc.put(":worried:", R.drawable.worried);
        emotionsKeySrc.put(":yum:", R.drawable.yum);
    }

    static {

        cityKeySrc.put("112", "马鞍山市");
        cityKeySrc.put("107", "合肥市");
        cityKeySrc.put("115", "芜湖市");
        cityKeySrc.put("101", "蚌埠市");
        cityKeySrc.put("109", "淮南市");

        cityKeySrc.put("108", "淮北市");
        cityKeySrc.put("114", "铜陵市");
        cityKeySrc.put("100", "安庆市");
        cityKeySrc.put("110", "黄山市");
        cityKeySrc.put("106", "阜阳市");
        cityKeySrc.put("113", "宿州市");
        cityKeySrc.put("103", "巢湖市");
        cityKeySrc.put("111", "六安市");
        cityKeySrc.put("102", "亳州市");
        cityKeySrc.put("104", "池州市");
        cityKeySrc.put("105", "滁州市");
        cityKeySrc.put("116", "宣城市");


        cityKeySrc.put("118", "福州市");
        cityKeySrc.put("125", "厦门市");
        cityKeySrc.put("121", "宁德市");
        cityKeySrc.put("122", "莆田市");
        cityKeySrc.put("123", "泉州市");
        cityKeySrc.put("126", "漳州市");
        cityKeySrc.put("119", "龙岩市");
        cityKeySrc.put("124", "三明市");
        cityKeySrc.put("120", "南平市");

        cityKeySrc.put("127", "白银市");
        cityKeySrc.put("128", "定西市");
        cityKeySrc.put("129", "甘南藏族自治州");
        cityKeySrc.put("130", "嘉峪关市");
        cityKeySrc.put("131", "金昌市");
        cityKeySrc.put("132", "酒泉市");
        cityKeySrc.put("133", "兰州市");
        cityKeySrc.put("134", "临夏回族自治州");
        cityKeySrc.put("135", "陇南市");
        cityKeySrc.put("136", "平凉市");
        cityKeySrc.put("137", "庆阳市");
        cityKeySrc.put("138", "天水市");
        cityKeySrc.put("139", "武威市");
        cityKeySrc.put("140", "张掖市");


        cityKeySrc.put("141", "潮州市");
        cityKeySrc.put("142", "东莞市");
        cityKeySrc.put("143", "佛山市");
        cityKeySrc.put("144", "广州市");
        cityKeySrc.put("145", "河源市");
        cityKeySrc.put("146", "惠州市");
        cityKeySrc.put("147", "江门市");
        cityKeySrc.put("148", "揭阳市");
        cityKeySrc.put("149", "茂名市");
        cityKeySrc.put("150", "梅州市");
        cityKeySrc.put("151", "清远市");
        cityKeySrc.put("152", "汕头市");
        cityKeySrc.put("153", "汕尾市");
        cityKeySrc.put("154", "韶关市");
        cityKeySrc.put("155", "深圳市");
        cityKeySrc.put("156", "阳江市");
        cityKeySrc.put("157", "云浮市");
        cityKeySrc.put("158", "湛江市");
        cityKeySrc.put("159", "肇庆市");
        cityKeySrc.put("160", "中山市");
        cityKeySrc.put("161", "珠海市");

        cityKeySrc.put("176", "安顺市");
        cityKeySrc.put("177", "毕节地区");
        cityKeySrc.put("178", "贵阳市");
        cityKeySrc.put("179", "六盘水市");
        cityKeySrc.put("180", "黔南布依族苗族自治州");
        cityKeySrc.put("181", "黔东南苗族侗族自治州");
        cityKeySrc.put("182", "黔西南布依族苗族自治州");
        cityKeySrc.put("183", "铜仁地区");
        cityKeySrc.put("184", "遵义市");

        cityKeySrc.put("185", "海口市");
        cityKeySrc.put("186", "三亚市");

        cityKeySrc.put("187", "保定市");
        cityKeySrc.put("188", "沧州市");
        cityKeySrc.put("189", "承德市");
        cityKeySrc.put("190", "邯郸市");
        cityKeySrc.put("191", "衡水市");
        cityKeySrc.put("192", "廊坊市");
        cityKeySrc.put("193", "秦皇岛");
        cityKeySrc.put("194", "石家庄");
        cityKeySrc.put("195", "唐山市");
        cityKeySrc.put("196", "邢台市");
        cityKeySrc.put("197", "张家口市");

        cityKeySrc.put("215", "大庆");
        cityKeySrc.put("216", "大兴安岭地区");
        cityKeySrc.put("217", "哈尔滨市");
        cityKeySrc.put("218", "鹤岗市");
        cityKeySrc.put("219", "黑河市");
        cityKeySrc.put("220", "鸡西市");
        cityKeySrc.put("221", "佳木斯市");
        cityKeySrc.put("222", "牡丹江市");
        cityKeySrc.put("223", "七台河市");
        cityKeySrc.put("224", "齐齐哈尔市");
        cityKeySrc.put("225", "双鸭山市");
        cityKeySrc.put("226", "绥化市");
        cityKeySrc.put("227", "伊春市");


        cityKeySrc.put("228", "鄂州市");
        cityKeySrc.put("229", "恩施土家族苗族自治州");
        cityKeySrc.put("230", "黄冈市");
        cityKeySrc.put("231", "黄石市");
        cityKeySrc.put("232", "荆门市");
        cityKeySrc.put("233", "荆州市");
        cityKeySrc.put("235", "十堰市");
        cityKeySrc.put("236", "随州市");
        cityKeySrc.put("237", "武汉市");
        cityKeySrc.put("238", "咸宁市");
        cityKeySrc.put("239", "襄樊市");
        cityKeySrc.put("240", "孝感市");
        cityKeySrc.put("241", "宜昌市");
        cityKeySrc.put("703", "江汉市");
        cityKeySrc.put("704", "天门市");
        cityKeySrc.put("705", "潜江市");
        cityKeySrc.put("706", "洪湖市");
        cityKeySrc.put("741", "仙桃市");
        cityKeySrc.put("742", "神农架");


        cityKeySrc.put("242", "长沙市");
        cityKeySrc.put("243", "常德市");
        cityKeySrc.put("244", "郴州市");
        cityKeySrc.put("245", "衡阳市");
        cityKeySrc.put("246", "怀化市");
        cityKeySrc.put("247", "娄底市");
        cityKeySrc.put("248", "邵阳市");
        cityKeySrc.put("249", "湘潭市");
        cityKeySrc.put("250", "湘西土家族苗族自治州");
        cityKeySrc.put("251", "益阳市");
        cityKeySrc.put("252", "永州市");
        cityKeySrc.put("253", "岳阳市");
        cityKeySrc.put("254", "张家界市");
        cityKeySrc.put("255", "株洲市");

        cityKeySrc.put("256", "白城市");
        cityKeySrc.put("257", "白山市");
        cityKeySrc.put("258", "长春市");
        cityKeySrc.put("259", "吉林市");
        cityKeySrc.put("260", "辽源市");
        cityKeySrc.put("261", "四平市");
        cityKeySrc.put("262", "松原市");
        cityKeySrc.put("263", "通化市");
        cityKeySrc.put("264", "延边朝鲜族自治州");

        cityKeySrc.put("265", "常州市");
        cityKeySrc.put("266", "淮安市");
        cityKeySrc.put("267", "连云港市");
        cityKeySrc.put("268", "南京市");
        cityKeySrc.put("269", "南通市");
        cityKeySrc.put("270", "苏州市");
        cityKeySrc.put("271", "宿迁市");
        cityKeySrc.put("272", "泰州市");
        cityKeySrc.put("273", "无锡市");
        cityKeySrc.put("274", "徐州市");
        cityKeySrc.put("275", "盐城市");
        cityKeySrc.put("276", "扬州市");
        cityKeySrc.put("277", "镇江市");


        cityKeySrc.put("278", "抚州市");
        cityKeySrc.put("279", "赣州市");
        cityKeySrc.put("280", "吉安市");
        cityKeySrc.put("281", "景德镇市");
        cityKeySrc.put("282", "九江市");
        cityKeySrc.put("283", "南昌市");
        cityKeySrc.put("284", "萍乡市");
        cityKeySrc.put("285", "上饶市");
        cityKeySrc.put("286", "新余市");
        cityKeySrc.put("287", "宜春市");
        cityKeySrc.put("288", "鹰潭市");


        cityKeySrc.put("289", "鞍山市");
        cityKeySrc.put("290", "本溪市");
        cityKeySrc.put("291", "朝阳市");
        cityKeySrc.put("292", "大连市");
        cityKeySrc.put("293", "丹东市");
        cityKeySrc.put("294", "抚顺市");
        cityKeySrc.put("295", "阜新市");
        cityKeySrc.put("296", "葫芦岛市");
        cityKeySrc.put("297", "锦州市");
        cityKeySrc.put("298", "辽阳市");
        cityKeySrc.put("299", "盘锦市");
        cityKeySrc.put("300", "沈阳市");
        cityKeySrc.put("301", "铁岭市");
        cityKeySrc.put("302", "营口市");

        cityKeySrc.put("320", "果洛藏族自治州");
        cityKeySrc.put("321", "海北藏族自治州");
        cityKeySrc.put("322", "海东地区");
        cityKeySrc.put("323", "海南藏族自治州");
        cityKeySrc.put("324", "海西蒙古族藏族自治州");
        cityKeySrc.put("325", "黄南藏族自治州");
        cityKeySrc.put("326", "西宁市");
        cityKeySrc.put("327", "玉树藏族自治州");
        cityKeySrc.put("743", "海西蒙古族藏族自治州");

        cityKeySrc.put("328", "滨州市");
        cityKeySrc.put("329", "德州市");
        cityKeySrc.put("330", "东营市");
        cityKeySrc.put("331", "菏泽市");
        cityKeySrc.put("332", "济南市");
        cityKeySrc.put("333", "济宁市");
        cityKeySrc.put("334", "莱芜市");
        cityKeySrc.put("335", "聊城市");
        cityKeySrc.put("336", "临沂市");
        cityKeySrc.put("337", "青岛市");
        cityKeySrc.put("338", "日照市");
        cityKeySrc.put("339", "泰安市");
        cityKeySrc.put("340", "威海市");
        cityKeySrc.put("341", "潍坊市");
        cityKeySrc.put("342", "烟台市");
        cityKeySrc.put("343", "枣庄市");
        cityKeySrc.put("344", "淄博市");

        cityKeySrc.put("345", "长治市");
        cityKeySrc.put("346", "大同市");
        cityKeySrc.put("347", "晋城市");
        cityKeySrc.put("348", "晋中市");
        cityKeySrc.put("349", "临汾市");
        cityKeySrc.put("350", "吕梁市");
        cityKeySrc.put("351", "朔州市");
        cityKeySrc.put("352", "太原市");
        cityKeySrc.put("353", "忻州市");
        cityKeySrc.put("354", "阳泉市");
        cityKeySrc.put("355", "运城市");

        cityKeySrc.put("356", "安康市");
        cityKeySrc.put("357", "宝鸡市");
        cityKeySrc.put("358", "汉中市");
        cityKeySrc.put("359", "商洛市");
        cityKeySrc.put("360", "铜川市");
        cityKeySrc.put("361", "渭南市");
        cityKeySrc.put("362", "西安市");
        cityKeySrc.put("363", "咸阳市");
        cityKeySrc.put("364", "延安市");
        cityKeySrc.put("365", "榆林市");

        cityKeySrc.put("410", "保山市");
        cityKeySrc.put("411", "楚雄彝族自治州");
        cityKeySrc.put("412", "大理白族自治州");
        cityKeySrc.put("413", "德宏傣族景颇族自治州");
        cityKeySrc.put("414", "迪庆藏族自治州");
        cityKeySrc.put("415", "红河哈尼族彝族自治州");
        cityKeySrc.put("416", "昆明市");
        cityKeySrc.put("417", "丽江市");
        cityKeySrc.put("418", "临沧市");
        cityKeySrc.put("419", "怒江傈傈族自治州");
        cityKeySrc.put("420", "曲靖市");
        cityKeySrc.put("421", "思茅市");
        cityKeySrc.put("422", "文山壮族苗族自治州");
        cityKeySrc.put("423", "西双版纳傣族自治州");
        cityKeySrc.put("424", "玉溪市");
        cityKeySrc.put("425", "昭通市");


        cityKeySrc.put("426", "杭州市");
        cityKeySrc.put("427", "湖州市");
        cityKeySrc.put("428", "嘉兴市");
        cityKeySrc.put("429", "金华市");
        cityKeySrc.put("430", "丽水市");
        cityKeySrc.put("431", "宁波市");
        cityKeySrc.put("432", "衢州市");
        cityKeySrc.put("433", "绍兴市");
        cityKeySrc.put("434", "台州市");
        cityKeySrc.put("435", "温州市");
        cityKeySrc.put("436", "舟山市");


        cityKeySrc.put("367", "阿坝藏族羌族自治州");
        cityKeySrc.put("368", "巴中市");
        cityKeySrc.put("369", "成都市");
        cityKeySrc.put("370", "达州市");
        cityKeySrc.put("371", "德阳市");
        cityKeySrc.put("372", "甘孜藏族自治州");
        cityKeySrc.put("373", "广安市");
        cityKeySrc.put("374", "广元市");
        cityKeySrc.put("375", "乐山市");
        cityKeySrc.put("376", "凉山彝族自治州");
        cityKeySrc.put("377", "泸州市");
        cityKeySrc.put("378", "眉山市");
        cityKeySrc.put("379", "绵阳市");
        cityKeySrc.put("380", "内江市");
        cityKeySrc.put("381", "南充市");
        cityKeySrc.put("382", "攀枝花市");
        cityKeySrc.put("383", "遂宁市");
        cityKeySrc.put("384", "雅安市");
        cityKeySrc.put("385", "宜宾市");
        cityKeySrc.put("386", "资阳市");
        cityKeySrc.put("387", "自贡市");


        cityKeySrc.put("198", "安阳市");
        cityKeySrc.put("199", "鹤壁市");
        cityKeySrc.put("200", "焦作市");
        cityKeySrc.put("201", "开封市");
        cityKeySrc.put("202", "洛阳市");
        cityKeySrc.put("203", "漯河市");
        cityKeySrc.put("204", "南阳市");
        cityKeySrc.put("205", "平顶山市");
        cityKeySrc.put("206", "濮阳市");
        cityKeySrc.put("207", "三门峡市");
        cityKeySrc.put("208", "商丘市");
        cityKeySrc.put("209", "新乡市");
        cityKeySrc.put("210", "信阳市");
        cityKeySrc.put("211", "许昌市");
        cityKeySrc.put("212", "郑州市");
        cityKeySrc.put("213", "周口市");
        cityKeySrc.put("214", "驻马店市");
        cityKeySrc.put("740", "济源市");


        cityKeySrc.put("162", "百色市");
        cityKeySrc.put("163", "北海市");
        cityKeySrc.put("164", "崇左市");
        cityKeySrc.put("165", "防城港市");
        cityKeySrc.put("166", "贵港市");
        cityKeySrc.put("167", "桂林市");
        cityKeySrc.put("168", "河池市");
        cityKeySrc.put("169", "贺州市");
        cityKeySrc.put("170", "来宾市");
        cityKeySrc.put("171", "柳州市");
        cityKeySrc.put("172", "南宁市");
        cityKeySrc.put("173", "钦州市");
        cityKeySrc.put("174", "梧州市");
        cityKeySrc.put("175", "玉林市");


        cityKeySrc.put("303", "阿拉善盟");
        cityKeySrc.put("304", "巴彦淖尔市");
        cityKeySrc.put("305", "包头市");
        cityKeySrc.put("306", "赤峰市");
        cityKeySrc.put("307", "鄂尔多斯市");
        cityKeySrc.put("308", "呼和浩特市");
        cityKeySrc.put("309", "呼伦贝尔市");
        cityKeySrc.put("310", "通辽市");
        cityKeySrc.put("311", "乌海市");
        cityKeySrc.put("312", "乌兰察布市");
        cityKeySrc.put("313", "锡林郭勒盟");
        cityKeySrc.put("314", "兴安盟");


        cityKeySrc.put("315", "固原市");
        cityKeySrc.put("316", "石嘴山市");
        cityKeySrc.put("317", "吴忠市");
        cityKeySrc.put("318", "银川市");
        cityKeySrc.put("319", "中卫市");


        cityKeySrc.put("389", "阿里地区");
        cityKeySrc.put("390", "昌都地区");
        cityKeySrc.put("391", "拉萨市");
        cityKeySrc.put("392", "林芝地区");
        cityKeySrc.put("393", "那曲地区");
        cityKeySrc.put("394", "日喀则地区");
        cityKeySrc.put("395", "山南地区");

        cityKeySrc.put("396", "阿克苏地区");
        cityKeySrc.put("397", "阿勒泰地区");
        cityKeySrc.put("398", "巴音郭楞蒙古自治州");
        cityKeySrc.put("399", "博尔塔拉蒙古自治州");
        cityKeySrc.put("400", "昌吉回族自治州");
        cityKeySrc.put("401", "哈密地区");
        cityKeySrc.put("402", "和田地区");
        cityKeySrc.put("403", "喀什地区");
        cityKeySrc.put("404", "克拉玛依市");
        cityKeySrc.put("405", "克孜勒苏柯尔克孜自治州");
        cityKeySrc.put("406", "塔城地区");
        cityKeySrc.put("407", "吐鲁番地区");
        cityKeySrc.put("408", "乌鲁木齐市");
        cityKeySrc.put("409", "伊犁哈萨克自治州");
        cityKeySrc.put("707", "奎屯");
        cityKeySrc.put("708", "乌苏");
        cityKeySrc.put("709", "石河子市");
        cityKeySrc.put("710", "沙湾");

        cityKeySrc.put("744", "阿拉尔市");
        cityKeySrc.put("745", "图木舒克市");
        cityKeySrc.put("746", "五家渠");


        cityKeySrc.put("438", "基隆市");
        cityKeySrc.put("439", "台南市");
        cityKeySrc.put("440", "台东");
        cityKeySrc.put("441", "台北市");
        cityKeySrc.put("442", "台中市");
        cityKeySrc.put("443", "高雄市");
        cityKeySrc.put("444", "新竹市");
        cityKeySrc.put("445", "嘉义市");


        cityKeySrc.put("724", "宜兰市");
        cityKeySrc.put("725", "桃园市");
        cityKeySrc.put("726", "新竹市");
        cityKeySrc.put("727", "苗栗县");
        cityKeySrc.put("728", "台中县");
        cityKeySrc.put("729", "彰化县");
        cityKeySrc.put("730", "南投县");
        cityKeySrc.put("731", "云林县");
        cityKeySrc.put("732", "嘉义县");
        cityKeySrc.put("733", "台南县");
        cityKeySrc.put("734", "高雄县");
        cityKeySrc.put("735", "屏东县");
        cityKeySrc.put("736", "台东县");
        cityKeySrc.put("737", "花莲县");
        cityKeySrc.put("738", "澎湖县");
        cityKeySrc.put("739", "台北县");

        cityKeySrc.put("445", "澳门特别行政区");
        cityKeySrc.put("444", "香港特别行政区");
        cityKeySrc.put("714", "其他国家");
        cityKeySrc.put("464", "上海市");
        cityKeySrc.put("702", "重庆市");
        cityKeySrc.put("684", "天津市");
        cityKeySrc.put("446", "北京市");
    }
    private static MyApplicaition INSTANCE;

    public static MyApplicaition INSTANCE() {
        return INSTANCE;
    }

    private void setInstance(MyApplicaition app) {
        setBmobIMApplication(app);
    }

    private static void setBmobIMApplication(MyApplicaition a) {
        MyApplicaition.INSTANCE = a;
    }


    /**
     * 获取当前运行的进程名
     *
     * @return
     */
    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
