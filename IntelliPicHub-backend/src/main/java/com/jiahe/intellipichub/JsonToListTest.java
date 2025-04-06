import cn.hutool.json.JSONUtil;
import java.util.List;

public class JsonToListTest {
    public static void main(String[] args) {
        // JSON 字符串，表示一个字符串数组
        String jsonStr = "['apple', 'banana', 'cherry']";

        // 将 JSON 字符串转换为 List<String>
        List<String> stringList = JSONUtil.toList(jsonStr, String.class);

        // 遍历并打印转换后的列表
        for (String item : stringList) {
            System.out.println(item);
        }
    }
}
