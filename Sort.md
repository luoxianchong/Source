# Sort

##### 引言：

```java
 Collections.sort(integers,new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return o2-o1; // 返回值为int类型，大于0表示正序，小于0表示逆序
     }
 });
//1、2、3、4、5、6 为正序

返回大于0：说明o2大于o1,o2需要在列表的后方。
```

##### 问：Collections.sort那种排序算法，是否问题，时间复杂度和空间复杂度分别是？



| 排序方法 | 时间复杂度（平均） | 时间复杂度（最坏） | 时间复杂度（最好） | 空间复杂度 | 稳定性 |
| -------- | ------------------ | ------------------ | ------------------ | ---------- | ------ |
| 冒泡排序 | O(n²)              | O(n²)              | O(n)               | O(1)       | 稳定   |
| 选择排序 | O(n²)              | O(n²)              | O(n²)              | O(1)       | 不稳定 |
| 插入排序 | O(n²)              | O(n²)              | O(n)               | O(1)       | 稳定   |
| 堆排序   | O(n㏒₂n)           | O(n㏒₂n)           | O(n㏒₂n)           | O(1)       | 不稳定 |
| 快速排序 | O(n㏒₂n)           | O(n²)              | O(n㏒₂n)           | O(n㏒₂n)   | 不稳定 |
| 希尔排序 | O(n²)              | O(n²)              | O(n)               | O(1)       | 不稳定 |
| 归并排序 | O(n㏒₂n)           | O(n㏒₂n)           | O(n㏒₂n)           | O(n)       | 稳定   |
| 计数排序 | O(n+K)             | O(n+K)             | O(n+K)             | O(n+K)     | 稳定   |
| 桶排序   | O(n+K)             | O(n²)              | O(n)               | O(n+K)     | 稳定   |
| 基数排序 | O(n*K)             | O(n*K)             | O(n*K)             | O(n+K)     | 稳定   |

<img src=".\image\sort.png" style="zoom: 50%;" />

##### 冒泡排序

![](.\image\sort\bubble.gif)

```
public void bubbleSort(Integer[] list){
   for(int i=0;i<list.length-i;i++){
   	 for(int j=0;j<list.length-i;j++){
   	 	if(list[j]>list[j+1]){
   	 		exchange(list,j,j+1);
   	 	}
   	 }
   }
}


public void exchange(Integer[] list,int i,int j){
		list[j]=list[j]^list[j+1];
   	 	list[j+1]=list[j]^list[j+1];
   	 	list[j]=list[j]^list[j+1];
}
```



##### 归并排序