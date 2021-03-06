package com.ethan.mlife.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.ethan.mlife.common.Constants;
import com.ethan.mlife.dao.AbstractBusDao;
import com.ethan.mlife.dao.MyBusFactory;
import com.ethan.mlife.entity.Line;
import com.ethan.mlife.entity.Station;
import com.ethan.mlife.util.HttpUtil;

/**
 * @author Ethan
 * 
 */
public class SuZhouBusJsoupImpl extends AbstractBusDao {

	/**
	 * 获取线路列表
	 * 
	 * @param line
	 * @return
	 */
	public List<Line> getBusLine(Line line) {
		List<Line> listLine = new ArrayList<Line>();

		try {
			// 设置查询线路编号
			busLinePostParams.put(searchBusLinePostParamName, line.getLineNo());
			Document doc = Jsoup.connect(busLineUrl).data(busLinePostParams)
					.post();

			Iterator<Element> iterator = doc.select(busMainContentRow)
					.iterator();
			// filter table head
			if (iterator.hasNext())
				iterator.next();
			while (iterator.hasNext()) {
				Element tr = iterator.next();
				Element a = tr.select("a").first();

				Line item = new Line();
				item.setLineNo(a.text().trim());
				item.setDirection(tr.select("td").last().text().trim());

				String strUrl = String.format("%s%s", busBaseUrl, a
						.attr("href").trim());
				item.setUrlLink(HttpUtil.urlEncode(strUrl));

				int intFirstIndex = item.getLineNo().indexOf(line.getLineNo());
				int intLastIndex = item.getLineNo().lastIndexOf(
						line.getLineNo());

				// 只显示线路只出现1次的
				if (intFirstIndex > -1 && intFirstIndex == intLastIndex) {
					for (NameValuePair nvp : HttpUtil.parseUrlQueryString(
							strUrl, urlEncode)) {
						item.setId(nvp.getName().equalsIgnoreCase(
								Constants.BUS_LINE_GUID) ? nvp.getValue()
								: UUID.randomUUID().toString());
					}
					listLine.add(item);
				}
			}
		} catch (Exception ex) {
			Log.e(SuZhouBusJsoupImpl.class.toString(), ex.toString());
		}

		return listLine;
	}

	/**
	 * 获取站台列表
	 * 
	 * @param station
	 *            站台名
	 * @return
	 */
	public List<Station> getBusStation(Station station) {
		List<Station> listStation = new ArrayList<Station>();

		try {

			// 设置查询站台名称
			busStationPostParams.put(searchBusStationPostParamName,
					station.getName());

			Document doc = Jsoup.connect(busStationUrl)
					.data(busStationPostParams).post();
			Iterator<Element> iterator = doc.select(busMainContentRow)
					.iterator();
			// filter table head
			if (iterator.hasNext())
				iterator.next();
			while (iterator.hasNext()) {
				Element tr = iterator.next();
				Element a = tr.select("a").first();
				Elements tds = tr.getElementsByTag("td");

				Station item = new Station();
				item.setCityRegion(MyBusFactory.getMyBus().getRegion());
				item.setId(UUID.randomUUID().toString());
				item.setUrlLink(HttpUtil.urlEncode(String.format("%s%s",
						busBaseUrl, a.attr("href").trim())));
				item.setName(a.text().trim());
				item.setScode(tds.get(1).text().trim());
				item.setDistrict(tds.get(2).text().trim());
				item.setStreet(tds.get(3).text().trim());
				item.setArea(tds.get(4).text().trim());
				item.setDirection(tds.get(5).text().trim());
				listStation.add(item);
			}
		} catch (Exception ex) {
			Log.e(SuZhouBusJsoupImpl.class.toString(), ex.toString());
		}
		if (listStation.isEmpty() && listStation.size() == 0) {
			return this.queryStationFromHis(station);
		}
		return listStation;
	}

	/**
	 * 获取线路站台
	 * 
	 * @param line
	 * @return
	 */
	public List<Station> getLineStation(Line line) {
		List<Station> list = new ArrayList<Station>();
		try {
			Document doc = Jsoup.connect(line.getUrlLink()).get();
			Iterator<Element> iterator = doc.select(busMainContentRow)
					.iterator();
			// filter table head
			if (iterator.hasNext())
				iterator.next();
			while (iterator.hasNext()) {
				Element tr = iterator.next();
				Element a = tr.select("a").first();
				Elements tds = tr.getElementsByTag("td");
				Station station = new Station();
				station.setUrlLink(HttpUtil.urlEncode(String.format("%s%s",
						busBaseUrl, a.attr("href").trim())));
				station.setName(a.text().trim());
				station.setScode(tds.get(1).text().trim());
				station.setVeNumber(tds.get(2).text().trim());
				station.setPassTime(tds.get(3).text().trim());
				list.add(station);
			}
		} catch (Exception ex) {
			Log.e(SuZhouBusJsoupImpl.class.toString(), ex.toString());
		}
		return list;
	}

	/**
	 * 获取站台线路
	 * 
	 * @param station
	 * @return
	 */
	public List<Line> getStationLine(Station station) {
		List<Line> list = new ArrayList<Line>();
		try {
			Document doc = Jsoup.connect(station.getUrlLink()).get();
			Iterator<Element> iterator = doc.select(busMainContentRow)
					.iterator();
			// filter table head
			iterator.next();
			while (iterator.hasNext()) {
				Element tr = iterator.next();
				Element a = tr.select("a").first();
				Elements tds = tr.getElementsByTag("td");

				Line line = new Line();
				line.setLineNo(a.text().trim());
				line.setDirection(tds.get(1).text().trim());
				line.setVeNumber(tds.get(2).text().trim());
				line.setUpdateTime(tds.get(3).text().trim());
				line.setSpacing(tds.get(4).text().trim());

				String strUrl = String.format("%s%s", busBaseUrl, a
						.attr("href").trim());
				line.setUrlLink(HttpUtil.urlEncode(strUrl));

				for (NameValuePair nvp : HttpUtil.parseUrlQueryString(strUrl,
						urlEncode)) {
					line.setId(nvp.getName().equalsIgnoreCase(
							Constants.BUS_LINE_GUID) ? nvp.getValue() : UUID
							.randomUUID().toString());
				}

				list.add(line);
			}
		} catch (Exception ex) {
			Log.e(SuZhouBusJsoupImpl.class.toString(), ex.toString());
		}
		return list;
	}
}
