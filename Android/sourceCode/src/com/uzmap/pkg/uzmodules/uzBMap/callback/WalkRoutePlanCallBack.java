/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.callback;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine.WalkingStep;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class WalkRoutePlanCallBack {
	private List<WalkingRouteLine> plans;

	public List<WalkingRouteLine> getPlans() {
		return plans;
	}

	public boolean routePlanCallBack(UZModuleContext moduleContext,
			WalkingRouteResult result) {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		if (result == null) {
			routeWalkPlanErr(moduleContext, ret, err, -1);
		} else if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
			suggest(moduleContext, result, ret, err, 1);
		} else if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			suggest(moduleContext, result, ret, err, 2);
		} else if (result.error == SearchResult.ERRORNO.NOT_SUPPORT_BUS) {
			routeWalkPlanErr(moduleContext, ret, err, 3);
		} else if (result.error == SearchResult.ERRORNO.NOT_SUPPORT_BUS_2CITY) {
			routeWalkPlanErr(moduleContext, ret, err, 4);
		} else if (result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			routeWalkPlanErr(moduleContext, ret, err, 5);
		} else if (result.error == SearchResult.ERRORNO.ST_EN_TOO_NEAR) {
			routeWalkPlanErr(moduleContext, ret, err, 6);
		} else if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			routeWalkPlanOk(moduleContext, result, ret);
			return true;
		} else {
			routeWalkPlanErr(moduleContext, ret, err, -1);
		}
		return false;
	}

	private void suggest(UZModuleContext moduleContext,
			WalkingRouteResult result, JSONObject ret, JSONObject err, int code) {
		try {
			ret.put("status", false);
			err.put("code", code);
			SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
			if (suggestAddrInfo != null) {
				List<PoiInfo> nodes = null;
				nodes = suggestAddrInfo.getSuggestStartNode();
				putSuggest(err, nodes, "suggestStarts");
				nodes = suggestAddrInfo.getSuggestEndNode();
				putSuggest(err, nodes, "suggestEnds");
			}
			moduleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void putSuggest(JSONObject err, List<PoiInfo> nodes, String name) {
		JSONArray suggesArray = new JSONArray();
		JSONObject suggestNode = null;
		try {
			if (nodes != null && nodes.size() > 0) {
				for (PoiInfo poiInfo : nodes) {
					suggestNode = new JSONObject();
					LatLng latLng = poiInfo.location;
					suggestNode.put("name", poiInfo.name);
					suggestNode.put("city", poiInfo.city);
					suggestNode.put("lat", latLng.latitude);
					suggestNode.put("lon", latLng.longitude);
					suggesArray.put(suggestNode);
				}
				err.put(name, suggesArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void routeWalkPlanOk(UZModuleContext moduleContext,
			WalkingRouteResult result, JSONObject ret) {
		try {
			List<WalkingRouteLine> plans = result.getRouteLines();
			if (plans != null && plans.size() > 0) {
				this.plans = plans;
				JSONArray plansJson = new JSONArray();
				ret.put("status", true);
				ret.put("plans", plansJson);
				fillPlans(plans, plansJson);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void fillPlans(List<WalkingRouteLine> plans, JSONArray plansJson) {
		JSONObject planJson = null;
		JSONObject startJson = null;
		JSONObject endJson = null;
		JSONArray array = null;
		JSONObject nodeJson = null;
		for (WalkingRouteLine plan : plans) {
			planJson = new JSONObject();
			startJson = new JSONObject();
			endJson = new JSONObject();
			array = new JSONArray();
			initCallBackJson(plansJson, planJson, startJson, endJson, array);
			putDisDur(plan, planJson);
			putStartEnd(plan, startJson, endJson);
			putSteps(plan, nodeJson, array);
		}
	}

	private void putDisDur(WalkingRouteLine plan, JSONObject planJson) {
		int distance = plan.getDistance();
		int duration = plan.getDuration();
		try {
			planJson.put("distance", distance);
			planJson.put("duration", duration);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void putStartEnd(WalkingRouteLine plan, JSONObject startJson,
			JSONObject endJson) {
		RouteNode startNode = plan.getStarting();
		nodeJson(startNode, startJson);
		RouteNode endNode = plan.getTerminal();
		nodeJson(endNode, endJson);
	}

	private void putSteps(WalkingRouteLine plan, JSONObject nodeJson,
			JSONArray array) {
		List<WalkingStep> steps = plan.getAllStep();
		for (WalkingStep step : steps) {
			nodeJson = new JSONObject();
			LatLng nodeLocation = step.getEntrance().getLocation();
			try {
				nodeJson.put("lon", nodeLocation.longitude);
				nodeJson.put("lat", nodeLocation.latitude);
				nodeJson.put("degree", step.getDirection());
				nodeJson.put("description", step.getInstructions());
				array.put(nodeJson);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void initCallBackJson(JSONArray plansJson, JSONObject planJson,
			JSONObject startJson, JSONObject endJson, JSONArray array) {
		try {
			planJson.put("start", startJson);
			planJson.put("end", endJson);
			planJson.put("nodes", array);
			plansJson.put(planJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void nodeJson(RouteNode node, JSONObject nodeJson) {
		LatLng location = node.getLocation();
		try {
			nodeJson.put("lon", location.longitude);
			nodeJson.put("lat", location.latitude);
			nodeJson.put("description", node.getTitle());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void routeWalkPlanErr(UZModuleContext moduleContext,
			JSONObject ret, JSONObject err, int code) {
		try {
			ret.put("status", false);
			err.put("code", -1);
			moduleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
