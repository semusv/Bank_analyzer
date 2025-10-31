import { handleApiResponse } from "../utils.js";

export async function fetchAnalyticsTimeSeries(filters = {}) {
    const params = new URLSearchParams();

    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);

    // operationTypeIds
    if (filters.operationTypeIds && Array.isArray(filters.operationTypeIds) && filters.operationTypeIds.length > 0) {
        params.append('operationTypeIdList', filters.operationTypeIds.join(','));
    }
    // Multiple card IDs: comma-separated
    if (filters.cardIds && Array.isArray(filters.cardIds) && filters.cardIds.length > 0) {
        params.append('cardIdList', filters.cardIds.join(','));
    }

    const queryString = params.toString();
    const url = `/api/analytics/time-series?${queryString}`;

    const response = await fetch(url, {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}

export async function fetchAnalyticsCategoryBreakdown(filters = {}) {
    const params = new URLSearchParams();

    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.operationType !== undefined && filters.operationType !== null && filters.operationType !== '') {
        params.append('operationType', filters.operationType);
    }

    if (filters.cardIds && Array.isArray(filters.cardIds) && filters.cardIds.length > 0) {
        params.append('cardIdList', filters.cardIds.join(','));
    }

    const url = `/api/analytics/category-breakdown?${params.toString()}`;
    const response = await fetch(url, {
        headers: {
            'Accept': 'application/json'
        }
    });
    return await handleApiResponse(response);
}


