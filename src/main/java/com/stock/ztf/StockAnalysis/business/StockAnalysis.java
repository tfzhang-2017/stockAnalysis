package com.stock.ztf.StockAnalysis.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.stock.ztf.StockAnalysis.mappers.StockDataAnalysisMapper;
import com.stock.ztf.StockAnalysis.utils.FnUtils;

@Service
public class StockAnalysis {

	private final static Logger logger = LoggerFactory.getLogger(StockAnalysis.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	private StockDataAnalysisMapper stockDataAnalysisMapper;	

	@Async
	public Map<String, Object> macdAnalysis(String year, String dType, String code) {
		String retStr = "";
		String retStatus = "";
		List<Map<String, Object>> stockDatas = new ArrayList<Map<String, Object>>();
		String[] years = year.split(",");
		for (String y : years) {
			stockDatas.addAll(stockDataAnalysisMapper.getTradeCMAData(y, dType, code));
		}
		/**
		 * 计算每日股价涨跌幅、MA5的涨跌幅
		 */
		for (int i = 0; i < stockDatas.size(); i++) {
			Map<String, Object> stockData = stockDatas.get(i);
			if (i != 0) {
				Map<String, Object> upStockData = stockDatas.get(i - 1);
				/**
				 * 计算股价涨跌幅
				 */
				float change = ((float) stockData.get("close") - (float) upStockData.get("close")) * 100
						/ (float) upStockData.get("close");
				/**
				 * 计算MA5的涨跌幅
				 */
				float ma5Change = ((float) stockData.get("MA5") - (float) upStockData.get("MA5")) * 100
						/ (float) upStockData.get("MA5");
				stockData.put("change", change);
				stockData.put("ma5Change", ma5Change);
			} else {
				stockData.put("change", 0.0f);
				stockData.put("ma5Change", 0.0f);
			}
			/**
			 * 计算DIFF和DEA的差值
			 */
			// float
			// ddChange=(float)stockData.get("DIFF")-(float)stockData.get("DEA");
			// stockData.put("ddChange", ddChange);
		}
		/**
		 * 根据MA5变化率统计股价涨跌幅
		 */
		List<Map<String, Object>> tongjiMA5Change = new ArrayList<Map<String, Object>>();
		Map<String, Object> changeMap = new HashMap<String, Object>();
		boolean isZheng = true;
		for (Map<String, Object> stockData : stockDatas) {
			if (changeMap.isEmpty()) {
				isZheng = ((float) stockData.get("ma5Change")) > 0;
				changeMap.put("isZheng", isZheng);
				changeMap.put("dateList", new ArrayList<String>() {
					{
						add(stockData.get("tradeDate").toString());
					}
				});
				changeMap.put("ma5Changes", new ArrayList<Float>() {
					{
						add((float) stockData.get("ma5Change"));
					}
				});
				changeMap.put("changeFu", (float) stockData.get("change"));
			} else {
				/**
				 * 同向涨跌幅相加
				 */
				if ((((float) stockData.get("ma5Change")) > 0) == isZheng) {
					((List<String>) (changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
					((List<Float>) (changeMap.get("ma5Changes"))).add((float) stockData.get("ma5Change"));
					changeMap.put("changeFu", (float) (changeMap.get("changeFu")) + (float) stockData.get("change"));
				} else {
					tongjiMA5Change.add(changeMap);
					changeMap = new HashMap<String, Object>();
					isZheng = ((float) stockData.get("ma5Change")) > 0;
					changeMap.put("isZheng", isZheng);
					changeMap.put("dateList", new ArrayList<String>() {
						{
							add(stockData.get("tradeDate").toString());
						}
					});
					changeMap.put("ma5Changes", new ArrayList<Float>() {
						{
							add((float) stockData.get("ma5Change"));
						}
					});
					changeMap.put("changeFu", (float) stockData.get("change"));
				}
			}
		}
		tongjiMA5Change.add(changeMap);
		/**
		 * 根据区间涨跌幅排序
		 */
		// Collections.sort(tongjiMA5Change, new Comparator<Map<String,
		// Object>>() {
		// public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		// return changeFu1.compareTo(changeFu2);
		// }
		// });
		/**
		 * 打印统计结果
		 */
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";
		float totalFu = 0f;
		int count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			if ((boolean) map.get("isZheng")) {
				// System.out.println("↑↑↑:changeFu="+map.get("changeFu")+
				// ", dateList="+map.get("dateList")+
				// ", ma5Changes="+map.get("ma5Changes")+
				// ", isZheng="+map.get("isZheng"));
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↑↑↑:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↑↑↑:count:" + count + ", totalFu:" + totalFu + "<br/>";
		totalFu = 0f;
		count = 0;
		for (Map<String, Object> map : tongjiMA5Change) {
			if (!(boolean) map.get("isZheng")) {
				// System.out.println("↓↓↓:changeFu="+map.get("changeFu")+
				// ", dateList="+map.get("dateList")+
				// ", ma5Changes="+map.get("ma5Changes")+
				// ", isZheng="+map.get("isZheng"));
				List<String> dateList = (List<String>) map.get("dateList");
				retStr += "↓↓↓:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
						+ "<br/>";
				totalFu += (float) map.get("changeFu");
				count += 1;
			}
		}
		retStr += "↓↓↓:count:" + count + ", totalFu:" + totalFu + "<br/>";
		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";

		Map<String, Object> zuihouData = tongjiMA5Change.get(tongjiMA5Change.size() - 1);
		List<String> tdateList = (List<String>) zuihouData.get("dateList");
		zuihouData.put("dateCount", tdateList.size());
		return zuihouData;
		// System.out.println("------------------------------根据MA5变化率统计------------------------------");

		/**
		 * 根据DIFF和DEA的差值统计股价涨跌幅
		 */
		//
		// List<Map<String, Object>> tongjiDDChange=new ArrayList<Map<String,
		// Object>>();
		// changeMap=new HashMap<String, Object>();
		// isZheng=true;
		// for (Map<String, Object> stockData : stockDatas) {
		// if (changeMap.isEmpty()) {
		// isZheng=((float)stockData.get("ddChange"))>0;
		// changeMap.put("isZheng", isZheng);
		// changeMap.put("dateList", new
		// ArrayList<String>(){{add(stockData.get("tradeDate").toString());}});
		// changeMap.put("ddChanges", new
		// ArrayList<Float>(){{add((float)stockData.get("ddChange"));}});
		// changeMap.put("changeFu", (float)stockData.get("change"));
		// }else{
		// /**
		// * 同向涨跌幅相加
		// */
		// if ((((float)stockData.get("ddChange"))>0)==isZheng) {
		// ((List<String>)(changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
		// ((List<Float>)(changeMap.get("ddChanges"))).add((float)stockData.get("ddChange"));
		// changeMap.put("changeFu",
		// (float)(changeMap.get("changeFu"))+(float)stockData.get("change"));
		// } else {
		// tongjiDDChange.add(changeMap);
		// changeMap=new HashMap<String, Object>();
		// isZheng=((float)stockData.get("ddChange"))>0;
		// changeMap.put("isZheng", isZheng);
		// changeMap.put("dateList", new
		// ArrayList<String>(){{add(stockData.get("tradeDate").toString());}});
		// changeMap.put("ddChanges", new
		// ArrayList<Float>(){{add((float)stockData.get("ddChange"));}});
		// changeMap.put("changeFu", (float)stockData.get("change"));
		// }
		// }
		// }
		// tongjiDDChange.add(changeMap);
		// /**
		// * 根据区间涨跌幅排序
		// */
		//// Collections.sort(tongjiDDChange, new Comparator<Map<String,
		// Object>>() {
		//// public int compare(Map<String, Object> o1, Map<String, Object> o2)
		// {
		//// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		//// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		//// return changeFu1.compareTo(changeFu2);
		//// }
		//// });
		// /**
		// * 打印统计结果
		// */
		//// System.out.println("------------------------------根据DIFF和DEA差值统计------------------------------");
		// retStr+="------------------------------根据DIFF和DEA差值统计------------------------------<br/>";
		// totalFu=0f;
		// count=0;
		// for (Map<String, Object> map : tongjiDDChange) {
		// if ((boolean)map.get("isZheng")) {
		//// System.out.println("↑↑↑:changeFu="+map.get("changeFu")+
		//// ", dateList="+map.get("dateList")+
		//// ", ddChanges="+map.get("ddChanges")+
		//// ", isZheng="+map.get("isZheng"));
		// List<String> dateList=(List<String>) map.get("dateList");
		// retStr+="↑↑↑:changeFu="+map.get("changeFu")+
		// ", dateCount="+dateList.size()+
		// ", dateList="+dateList+
		// ", ddChanges="+map.get("ddChanges")+
		// ", isZheng="+map.get("isZheng")+"<br/>";
		// totalFu+=(float)map.get("changeFu");
		// count+=1;
		// }
		// }
		// retStr+="↑↑↑:count:"+count+", totalFu:"+totalFu+"<br/>";
		//// System.out.println("count:"+count+", totalFu:"+totalFu);
		// totalFu=0f;
		// count=0;
		// for (Map<String, Object> map : tongjiDDChange) {
		// if (!(boolean)map.get("isZheng")) {
		//// System.out.println("↓↓↓:changeFu="+map.get("changeFu")+
		//// ", dateList="+map.get("dateList")+
		//// ", ddChanges="+map.get("ddChanges")+
		//// ", isZheng="+map.get("isZheng"));
		// List<String> dateList=(List<String>) map.get("dateList");
		// retStr+="↓↓↓:changeFu="+map.get("changeFu")+
		// ", dateCount="+dateList.size()+
		// ", dateList="+dateList+
		// ", ddChanges="+map.get("ddChanges")+
		// ", isZheng="+map.get("isZheng")+"<br/>";
		// totalFu+=(float)map.get("changeFu");
		// count+=1;
		// }
		// }
		//// System.out.println("count:"+count+", totalFu:"+totalFu);
		// retStr+="↓↓↓:count:"+count+", totalFu:"+totalFu+"<br/>";
		// retStr+="------------------------------根据DIFF和DEA差值统计------------------------------<br/>";
		//// System.out.println("------------------------------根据DIFF和DEA差值统计------------------------------");
		// return null;
	}

	/**
	 * 根据MA分析股票，并将结果记入数据库
	 * @param year
	 * @param dType
	 * @param code
	 * @return Map<String, Object>
	 */
	@Async
	synchronized public Map<String, Object> maAnalysis(String year, String dType, String code) {
		String retStr = "";
		String retStatus = "";
		List<Map<String, Object>> stockDatas = new ArrayList<Map<String, Object>>();
		String[] years = year.split(",");
		for (String y : years) {
			stockDatas.addAll(stockDataAnalysisMapper.getTradeCMAData(y, dType, code));
		}
		
		logger.debug("get code:"+code+" stockDatas " + stockDatas.size() + " records");
		/**
		 * 计算每日股价涨跌幅、MA5的涨跌幅
		 */
		for (int i = 0; i < stockDatas.size(); i++) {
			Map<String, Object> nowStockData = stockDatas.get(i);
			if (i != 0) {
				Map<String, Object> prevStockData = stockDatas.get(i - 1);
				/**
				 * 计算股价涨跌幅
				 */
				float change = ((float) nowStockData.get("close") - (float) prevStockData.get("close")) * 100
						/ (float) prevStockData.get("close");
				/**
				 * 计算MA5的涨跌幅
				 */
				float ma5Change = ((float) nowStockData.get("MA5") - (float) prevStockData.get("MA5")) * 100
						/ (float) prevStockData.get("MA5");
				nowStockData.put("change", change);
				nowStockData.put("ma5Change", ma5Change);
			} else {
				/*
				 * 处理上市当天数据
				 */
				nowStockData.put("change", 0.0f);
				nowStockData.put("ma5Change", 0.0f);
			}

		}
		/**
		 * 根据MA5涨跌幅统计股价涨跌幅
		 */
		List<Map<String, Object>> tongjiMA5Change = new ArrayList<Map<String, Object>>();
		Map<String, Object> changeMap = new HashMap<String, Object>();
		boolean isZheng = true;
		boolean ma5Zheng = true;
		for (Map<String, Object> stockData : stockDatas) {
			if (changeMap.isEmpty()) {
				/*
				 * 处理第一个数据
				 */
				isZheng = ((float) stockData.get("ma5Change")) > 0;
				changeMap.put("isZheng", isZheng);
				changeMap.put("dateList", new ArrayList<String>() {
					{
						add(stockData.get("tradeDate").toString());
					}
				});
				changeMap.put("ma5Changes", new ArrayList<Float>() {
					{
						add((float) stockData.get("ma5Change"));
					}
				});
				changeMap.put("changeFu", (float) stockData.get("change"));
				changeMap.put("startDate", (String) stockData.get("tradeDate"));
				changeMap.put("endDate", (String) stockData.get("tradeDate"));
			} else {

				ma5Zheng = (float) stockData.get("ma5Change") > 0;
				if (ma5Zheng == isZheng) {
					/**
					 * 同向涨跌幅相加
					 */
					((List<String>) (changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
					((List<Float>) (changeMap.get("ma5Changes"))).add((float) stockData.get("ma5Change"));
					changeMap.put("changeFu", (float) (changeMap.get("changeFu")) + (float) stockData.get("change"));
					changeMap.replace("endDate", (String) stockData.get("tradeDate"));
				} else {
					tongjiMA5Change.add(changeMap);
					changeMap = new HashMap<String, Object>();
					isZheng = ((float) stockData.get("ma5Change")) > 0;
					changeMap.put("isZheng", isZheng);
					changeMap.put("dateList", new ArrayList<String>() {
						{
							add(stockData.get("tradeDate").toString());
						}
					});
					changeMap.put("ma5Changes", new ArrayList<Float>() {
						{
							add((float) stockData.get("ma5Change"));
						}
					});
					changeMap.put("changeFu", (float) stockData.get("change"));
					changeMap.put("startDate", (String) stockData.get("tradeDate"));
					changeMap.put("endDate", (String) stockData.get("tradeDate"));
				}
			}
		}
		tongjiMA5Change.add(changeMap);
		/*
		 * 分析结果记入数据库
		 */
		logger.debug("get code:"+code+" tongjiMA5Change " + tongjiMA5Change.size() + " records");
		for (Map<String, Object> cMap : tongjiMA5Change) {
			Map<String, Object> data=new HashMap<String, Object>();
			data.put("code", code);
			data.put("isZheng", cMap.get("isZheng"));
			List<String> dateList=((List<String>)(cMap.get("dateList")));
			data.put("dateCount",dateList.size());
			data.put("dateList",String.join(",", dateList));
			data.put("ma5Changes",String.join(",", FnUtils.toStringList(((List<String>)(cMap.get("ma5Changes"))))));
			data.put("changeFu", cMap.get("changeFu"));
			data.put("dateType", dType);
			data.put("startDate", cMap.get("startDate"));
			data.put("endDate", cMap.get("endDate"));
			stockDataAnalysisMapper.insertOrUpdateAnalysisData(data);
		}
				
		/**
		 * 根据区间涨跌幅排序
		 */
		// Collections.sort(tongjiMA5Change, new Comparator<Map<String,
		// Object>>() {
		// public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		// Float changeFu1 = Float.valueOf(o1.get("changeFu").toString()) ;
		// Float changeFu2 = Float.valueOf(o2.get("changeFu").toString()) ;
		// return changeFu1.compareTo(changeFu2);
		// }
		// });
		/**
		 * 打印统计结果
		 */
//		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";
//		for (Map<String, Object> map : tongjiMA5Change) {
//			if (!(boolean) map.get("isZheng")) {
//				System.out.println("↓↓↓:changeFu=" + map.get("changeFu") + ", startDate=" + map.get("startDate")
//						+ ", endDate=" + map.get("endDate") + ", ma5Changes=" + map.get("ma5Changes"));
//			}
//		}
//		for (Map<String, Object> map : tongjiMA5Change) {
//
//			if ((boolean) map.get("isZheng")) {
//				System.out.println("↑↑↑:changeFu=" + map.get("changeFu") + ", startDate=" + map.get("startDate")
//						+ ", endDate=" + map.get("endDate") + ", ma5Changes=" + map.get("ma5Changes"));
//			}
//		}
//		float totalFu = 0f;
//		int count = 0;
//		for (Map<String, Object> map : tongjiMA5Change) {
//			/*
//			 * 打印上涨数据
//			 */
//			if ((boolean) map.get("isZheng")) {
//				List<String> dateList = (List<String>) map.get("dateList");
//				retStr += "↑↑↑:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
//						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
//						+ "<br/>";
//				totalFu += (float) map.get("changeFu");
//				count += 1;
//			}
//		}
//		retStr += "↑↑↑:count:" + count + ", totalFu:" + totalFu + "<br/>";
//		totalFu = 0f;
//		count = 0;
//		for (Map<String, Object> map : tongjiMA5Change) {
//			/*
//			 * 打印下跌数据
//			 */
//			if (!(boolean) map.get("isZheng")) {
//				List<String> dateList = (List<String>) map.get("dateList");
//				retStr += "↓↓↓:changeFu=" + map.get("changeFu") + ", dateCount=" + dateList.size() + ", dateList="
//						+ dateList + ", ma5Changes=" + map.get("ma5Changes") + ", isZheng=" + map.get("isZheng")
//						+ "<br/>";
//				totalFu += (float) map.get("changeFu");
//				count += 1;
//			}
//		}
//		retStr += "↓↓↓:count:" + count + ", totalFu:" + totalFu + "<br/>";
//		retStr += "------------------------------根据MA5变化率统计------------------------------<br/>";
//
		Map<String, Object> zuihouData = tongjiMA5Change.get(tongjiMA5Change.size() - 1);
		List<String> tdateList = (List<String>) zuihouData.get("dateList");
		zuihouData.put("dateCount", tdateList.size());
		return zuihouData;
		// System.out.println("------------------------------根据MA5变化率统计------------------------------");
	}
	
	/**
	 * 根据MA变化率买卖股票，并打印收益
	 * @param year
	 * @param dType
	 * @param code
	 * @return Map<String, Object>
	 */
	@Async
	synchronized public void cal_stockShouYiByCma(String year, String dType, String code) {
		String retStr = "";
		String retStatus = "";
		List<Map<String, Object>> stockDatas = new ArrayList<Map<String, Object>>();
		String[] years = year.split(",");
		for (String y : years) {
			stockDatas.addAll(stockDataAnalysisMapper.getTradeCMAData(y, dType, code));
		}
		
		logger.debug("get code:"+code+" stockDatas " + stockDatas.size() + " records");
		/**
		 * 计算每日股价涨跌幅、MA5的涨跌幅
		 */
		for (int i = 0; i < stockDatas.size(); i++) {
			Map<String, Object> nowStockData = stockDatas.get(i);
			if (i != 0) {
				Map<String, Object> prevStockData = stockDatas.get(i - 1);
				/**
				 * 前一日收盘价
				 */
				float prevClose = (float) prevStockData.get("close");
				/**
				 * 今日收盘价
				 */
				float curClose = (float) nowStockData.get("close");
				/*
				 * 今日跌停价
				 */
				float dieClose = FnUtils.floatRound(prevClose-prevClose*0.1f,2);
				nowStockData.put("dieClose", dieClose);
				/*
				 * 今日涨停价
				 */
				float zhangClose = FnUtils.floatRound(prevClose+prevClose*0.1f,2);
				nowStockData.put("zhangClose", zhangClose);
				/*
				 * 今日中间价
				 */
				float zhongClose = (prevClose+curClose)/2;
				nowStockData.put("zhongClose", zhongClose);
				
				/**
				 * 计算MA5的涨跌幅
				 */
			
				float ma5Change = ((float) nowStockData.get("MA5") - (float) prevStockData.get("MA5")) * 100
						/ (float) prevStockData.get("MA5");
				nowStockData.put("ma5Change", ma5Change);
				if (curClose>=zhangClose) {
					nowStockData.put("zhangTing", 1);
				}else{
					nowStockData.put("zhangTing", 0);
				}
				if (curClose<=dieClose) {
					nowStockData.put("dieTing", 1);
				}else {
					nowStockData.put("dieTing", 0);
				}
			} else {
				/*
				 * 处理上市当天数据
				 */
				nowStockData.put("ma5Change", 0.0f);
				nowStockData.put("zhangTing", 1);
				nowStockData.put("dieTing", 1);
			}

		}
		/**
		 * 根据MA5涨跌幅统计股价涨跌幅
		 */
		List<Map<String, Object>> tongjiMA5Change = new ArrayList<Map<String, Object>>();
		Map<String, Object> changeMap = new HashMap<String, Object>();
		boolean isZheng = true;
		boolean ma5Zheng = true;
		for (Map<String, Object> stockData : stockDatas) {
			int zhangTing=(int)stockData.get("zhangTing");
			int dieTing=(int)stockData.get("dieTing");
			if (changeMap.isEmpty()) {
				/*
				 * 涨跌停处理
				 */
				if (zhangTing==1||dieTing==1) {
					continue;
				}
				/*
				 * 处理第一个数据
				 */
				isZheng = ((float) stockData.get("ma5Change")) > 0;
				changeMap.put("isZheng", isZheng);
				changeMap.put("dateList", new ArrayList<String>() {
					{
						add(stockData.get("tradeDate").toString());
					}
				});
				changeMap.put("ma5Changes", new ArrayList<Float>() {
					{
						add((float) stockData.get("ma5Change"));
					}
				});
				changeMap.put("maiRuJia", (float) stockData.get("close"));
				changeMap.put("maiChuJia", (float) stockData.get("close"));
				changeMap.put("startDate", (String) stockData.get("tradeDate"));
				changeMap.put("endDate", (String) stockData.get("tradeDate"));
			} else {

				ma5Zheng = (float) stockData.get("ma5Change") > 0;
				if (ma5Zheng == isZheng) {
					/**
					 * 同向涨跌幅相加
					 */
					((List<String>) (changeMap.get("dateList"))).add(stockData.get("tradeDate").toString());
					((List<Float>) (changeMap.get("ma5Changes"))).add((float) stockData.get("ma5Change"));
					changeMap.replace("endDate", (String) stockData.get("tradeDate"));
				} else {
					/*
					 * 涨跌停处理
					 */
					if (zhangTing==1||dieTing==1) {
						continue;
					}
					/*
					 * 记录卖出价
					 */
					changeMap.replace("maiChuJia", (float) stockData.get("close"));
					changeMap.replace("endDate", (String) stockData.get("tradeDate"));
					tongjiMA5Change.add(changeMap);
					changeMap = new HashMap<String, Object>();
					isZheng = ((float) stockData.get("ma5Change")) > 0;
					changeMap.put("isZheng", isZheng);
					changeMap.put("dateList", new ArrayList<String>() {
						{
							add(stockData.get("tradeDate").toString());
						}
					});
					changeMap.put("ma5Changes", new ArrayList<Float>() {
						{
							add((float) stockData.get("ma5Change"));
						}
					});
					changeMap.put("maiRuJia", (float) stockData.get("close"));
					changeMap.put("maiChuJia", (float) stockData.get("close"));
					changeMap.put("startDate", (String) stockData.get("tradeDate"));
					changeMap.put("endDate", (String) stockData.get("tradeDate"));
				}
			}			
		}
		tongjiMA5Change.add(changeMap);
		/*
		 * 打印收益结果
		 */
		logger.debug("get code:"+code+" tongjiMA5Change " + tongjiMA5Change.size() + " records");
		for (Map<String, Object> cMap : tongjiMA5Change) {
			float maiRuJia=(float)cMap.get("maiRuJia");
			float maiChuJia=(float)cMap.get("maiChuJia");
			isZheng=(boolean) cMap.get("isZheng");
			List<String> dateList=((List<String>) (cMap.get("dateList")));
			float shouYiLv=(float) java.lang.StrictMath.pow(maiChuJia/maiRuJia, 1.0/dateList.size());
			if (isZheng) {
				shouYiLv=FnUtils.floatRound((shouYiLv-1)*100*dateList.size(), 2);
			} else {
				shouYiLv=FnUtils.floatRound((1-shouYiLv)*100*dateList.size(), 2);
			}
			cMap.put("code", code);
			cMap.put("shouYiLv", shouYiLv);
			cMap.put("dateCount", dateList.size());
//			System.out.println(
//					"code:"+code+
//					":shouYiLv:"+shouYiLv+
//					":isZheng:"+cMap.get("isZheng")+
//					":maiRuJia:"+cMap.get("maiRuJia")+
//					":maiChuJia:"+cMap.get("maiChuJia")+
//					":startDate:"+cMap.get("startDate")+
//					":endDate:"+cMap.get("endDate"));
			stockDataAnalysisMapper.insertOrUpdateStockShouYiData(cMap);
		}					
	}
}
