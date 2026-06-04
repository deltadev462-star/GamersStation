import { apiRequest } from '../config/api';

class RegionService {
  async getRegions() {
    try {
      const response = await apiRequest('/regions', {
        method: 'GET',
      });
      return response;
    } catch (error) {
      console.error('Error fetching regions:', error);
      throw error;
    }
  }
}

const regionService = new RegionService();

export { regionService };
export default regionService;
