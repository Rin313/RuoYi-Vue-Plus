* 当需要确认“是否有数据被实际修改”来决定下一步流程（如乐观锁、扣库存），才使用int返回值(受影响的行数)，insert和常规的update/delete一般直接调用
* 只使用分页查询（具体说明待详写）

TODO
- 移植xml
- LONG类型我草泥马，怎么前端处理不了的


model,output price,通用能力，指令遵从
qwen-turbo-2025-07-15,0.6￥,48.5,59.8，不再更新
Doubao-1.5-lite-32k-250115,0.6￥,48.3,64.0
qwen-flash-2025-07-28,1.5￥,53.8,62.8

hunyuan-turbos-20250926,2￥,61.6,71.7
qwen-plus-2025-07-28,2￥,64.7,70.1
doubao-seed-1-6-lite-251015，2.4￥,61.0,70.6
DeepSeek-V3.2-Think,3￥,68.4,74.7
DeepSeek-V3.2-Exp-Think,3￥,68.5,77.2
gemini-2.5-flash,18.125￥,60.0,65.0
https://huggingface.co/Tongyi-Zhiwen/QwenLong-L1.5-30B-A3B 最近更新过，看huggingface的图，大概有gemini-2.5-pro的水平





0.5*7.04=3.52 / (1000000/1000token)=0.00352
3*7.04=21.12 / (10000000/256token)=0.00540
0.0089，一块钱真能玩100条吗？


❌hunyuan-2.0-instruct-20251111,2￥,63.1,57.8