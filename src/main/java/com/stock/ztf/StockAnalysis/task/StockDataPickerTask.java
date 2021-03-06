package com.stock.ztf.StockAnalysis.task;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.stock.ztf.StockAnalysis.beans.TradeCMADataInfo;
import com.stock.ztf.StockAnalysis.business.IndustryData;
import com.stock.ztf.StockAnalysis.business.StockDataPicker;
import com.stock.ztf.StockAnalysis.mappers.StockBaseDataMapper;

/**
 * 采集股票基础数据
 * 
 * @author ztf
 *
 */
@Service
public class StockDataPickerTask {

	private final static Logger logger = LoggerFactory.getLogger(StockDataPickerTask.class);

	private static final int oneSecond = 1000;

	private static final int oneMinute = 60 * oneSecond;

	@Autowired
	private StockBaseDataMapper stockBaseDataMapper;

	@Autowired
	private StockDataPicker stockDataPicker;
	
	@Autowired
	private IndustryData industryData;

	/**
	 * 获取股票交易历史数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	// @Scheduled(cron = "0 0 9-15 * * ?")
	public void pickerStockTradeBaseData() {
//		List<Map<String, Object>> codeDatas = stockBaseDataMapper.getHYStockCodeData("^0|^6");
//		for (Map<String, Object> codes : codeDatas) {
//			String code = (String) codes.get("code");
//			String market = (String) codes.get("market");
			String code = "601628";
			String market = "1";
						
			stockDataPicker.pickerStockBaseData(code, market);
//		}
	}
	
	/**
	 * 获取行业交易历史数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	// @Scheduled(cron = "0 0 9-15 * * ?")
	public void pickerHYTradeBaseData() {
		List<String> codeDatas = stockBaseDataMapper.getHYCode();
		for (String hyCode : codeDatas) {
			String code = hyCode;
			String market = "1";
			// logger.debug("start get " + code + " day Trade real data ");
			// pickerStockTradeRealData(code, market);
			logger.debug("start get " + code + " day Trade Base data ");
			stockDataPicker.pickerStockTradeBaseData(code, market, "K");
			// logger.debug("start get "+code+" weekday Trade Base data ");
			// pickerStockTradeBaseData(code,market,"weekday","wk");
			// logger.debug("start get "+code+" month Trade Base data ");
			// pickerStockTradeBaseData(code,market,"month","mk");
			logger.debug("start get " + code + " day Trade cam data ");
			stockDataPicker.pickerStockTradeZhiBiaoData(code, market, "K", "cma",new TradeCMADataInfo());
			// logger.debug("start get " + code + " day Trade macd data ");
			// pickerStockTradeZhiBiaoData(code, market, "K", "macd", "day",
			// TradeMACDDataInfo.class);
		}
	}

	/**
	 * 获取自选股票交易历史数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	// @Scheduled(cron = "0 0 9-15 * * ?")
	public void pickerCustomStockTradeBaseData() {
		// List<String> codeDatas =
		// stockBaseDataMapper.getStockCodeData("^0|^6");
		// for (String code : codeDatas) {
		// String market = "1";
		// if (code.startsWith("00")) {
		// market = "2";
		// }
		//// logger.debug("start get " + code + " day Trade real data ");
		//// pickerStockTradeRealData(code, market);
		// logger.debug("start get " + code + " day Trade Base data ");
		// pickerStockTradeBaseData(code, market, "day", "K");
		// // logger.debug("start get "+code+" weekday Trade Base data ");
		// // pickerStockTradeBaseData(code,market,"weekday","wk");
		// // logger.debug("start get "+code+" month Trade Base data ");
		// // pickerStockTradeBaseData(code,market,"month","mk");
		// logger.debug("start get " + code + " day Trade cam data ");
		// pickerStockTradeZhiBiaoData(code, market, "K", "cma", "day",
		// TradeCMADataInfo.class);
		//// logger.debug("start get " + code + " day Trade macd data ");
		//// pickerStockTradeZhiBiaoData(code, market, "K", "macd", "day",
		// TradeMACDDataInfo.class);
		// }
		// List<String> hyCodeDatas = stockBaseDataMapper.getHYCode();
		// for (String hyCode : hyCodeDatas) {
		//// logger.debug("start get " + hyCode + " day Trade Base data ");
		//// pickerStockTradeBaseData(hyCode, "1", "day", "K");
		//// logger.debug("start get " + hyCode + " day Trade cam data ");
		//// pickerStockTradeZhiBiaoData(hyCode, "1", "K", "cma", "day",
		// TradeCMADataInfo.class);
		//// logger.debug("start get " + hyCode + " day Trade macd data ");
		//// pickerStockTradeZhiBiaoData(hyCode, "1", "K", "macd", "day",
		// TradeMACDDataInfo.class);
		// }
	}

	/**
	 * 获取行业股票列表数据
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerStockTradeHYStocksData() {
		List<String> hyCodes = stockBaseDataMapper.getHYCode();
		for (String hyCode : hyCodes) {
			industryData.pickerStockList(hyCode);
		}
	}

	/**
	 * 获取每日行业交易数据
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerHYTradeData() {
		logger.debug("get HY Real Trade data start");
		stockDataPicker.pickerStockTradeMRHYData();
		logger.debug("get HY Real Trade data end ");
	}

	/**
	 * 从文件读取股票代码数据，插入数据库
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 6000 * oneMinute)
	public void pickerStockCodeData() {
		logger.debug("Insert Stock Code data start");
		stockDataPicker.pickerStockCodeData();
		logger.debug("Insert Stock Code data end");
	}

	/**
	 * 获取行业股票投资评级数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerHYStockCTData() {
		List<String> hyCodes = stockBaseDataMapper.getHYCode();
		for (String hyCode : hyCodes) {
//			stockDataPicker.pickerHYStocCTData(hyCode);
		}
	}
	
	/**
	 * 获取行业历史资金数据
	 */
	// @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerHYHisZJData() {
		List<String> hyCodes = stockBaseDataMapper.getHYCode();
		for (String hyCode : hyCodes) {
			//industryData..pickerStockTradeHYStockData(hyCode);
		}
	}
	
	/**
	 * 获取股票历史资金数据
	 */
//	 @Scheduled(initialDelay = 5 * oneSecond, fixedRate = 2000 * oneMinute)
	public void pickerStockHisZJData() {
		List<Map<String, Object>> codeDatas = stockBaseDataMapper.getHYStockCodeData("^0|^6");
		for (Map<String, Object> codes : codeDatas) {
			String code = (String) codes.get("code");
			String market = (String) codes.get("market");
			logger.debug("get " + code + " day HY ZJ data ");
			stockDataPicker.pickerStockZJLSData(code, market);
		}
	}
	
}

