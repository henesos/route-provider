import { useState, useEffect } from 'react';
import { routesAPI, locationsAPI } from '../services/api';

const RoutesPage = () => {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [routes, setRoutes] = useState([]);
  const [error, setError] = useState('');
  const [searchParams, setSearchParams] = useState({
    originLocationId: '',
    destinationLocationId: '',
    travelDate: '',
  });

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        const response = await locationsAPI.getAll(0, 1000);
        setLocations(response.data.content);
      } catch (err) {
        setError('Failed to load locations');
      } finally {
        setLoading(false);
      }
    };
    fetchLocations();
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchParams.originLocationId || !searchParams.destinationLocationId || !searchParams.travelDate) {
      setError('Please fill in all fields');
      return;
    }

    setSearching(true);
    setError('');
    setRoutes([]);

    try {
      const response = await routesAPI.search(
        searchParams.originLocationId,
        searchParams.destinationLocationId,
        searchParams.travelDate
      );
      setRoutes(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to search routes');
    } finally {
      setSearching(false);
    }
  };

  const getTypeStyle = (type) => {
    const styles = {
      FLIGHT: 'bg-blue-100 text-blue-600 border-blue-200',
      BUS: 'bg-green-100 text-green-600 border-green-200',
      SUBWAY: 'bg-purple-100 text-purple-600 border-purple-200',
      UBER: 'bg-amber-100 text-amber-600 border-amber-200',
    };
    return styles[type] || 'bg-gray-100 text-gray-600 border-gray-200';
  };

  const getTypeIcon = (type) => {
    const icons = { FLIGHT: '✈️', BUS: '🚌', SUBWAY: '🚇', UBER: '🚕' };
    return icons[type] || '🚗';
  };

  const getTypeLabel = (type) => {
    const labels = { FLIGHT: 'Flight', BUS: 'Bus', SUBWAY: 'Metro', UBER: 'Taxi' };
    return labels[type] || type;
  };

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString('en-US', { 
      weekday: 'short', month: 'short', day: 'numeric' 
    });
  };

  const getTomorrowDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  };

  const getLocationById = (id) => locations.find(loc => loc.id === id);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  const origin = getLocationById(searchParams.originLocationId);
  const dest = getLocationById(searchParams.destinationLocationId);

  return (
    <div className="max-w-4xl mx-auto">
      {/* Search Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Search Routes</h2>
        
        <form onSubmit={handleSearch}>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">From</label>
              <select
                value={searchParams.originLocationId}
                onChange={(e) => setSearchParams({ ...searchParams, originLocationId: parseInt(e.target.value) || '' })}
                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none bg-white"
                required
              >
                <option value="">Select origin...</option>
                {locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>{loc.locationCode} - {loc.name}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">To</label>
              <select
                value={searchParams.destinationLocationId}
                onChange={(e) => setSearchParams({ ...searchParams, destinationLocationId: parseInt(e.target.value) || '' })}
                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none bg-white"
                required
              >
                <option value="">Select destination...</option>
                {locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>{loc.locationCode} - {loc.name}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Date</label>
              <input
                type="date"
                value={searchParams.travelDate}
                onChange={(e) => setSearchParams({ ...searchParams, travelDate: e.target.value })}
                min={getTomorrowDate()}
                className="w-full px-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                required
              />
            </div>
          </div>

          {error && (
            <div className="mt-4 p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={searching}
            className="mt-4 w-full md:w-auto px-8 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 font-medium"
          >
            {searching ? 'Searching...' : 'Search'}
          </button>
        </form>
      </div>

      {/* Results */}
      {routes.length > 0 && (
        <div>
          {/* Results Header */}
          <div className="flex items-center justify-between mb-4">
            <div>
              <span className="text-2xl font-bold text-gray-900">{routes.length}</span>
              <span className="text-gray-600 ml-1">route{routes.length > 1 ? 's' : ''} found</span>
            </div>
            {origin && dest && (
              <div className="text-sm text-gray-500">
                {origin.locationCode} → {dest.locationCode} • {formatDate(searchParams.travelDate)}
              </div>
            )}
          </div>

          {/* Route Cards */}
          <div className="space-y-4">
            {routes.map((route, idx) => (
              <div key={idx} className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                {/* Route Header */}
                <div className="px-5 py-3 bg-gray-50 border-b border-gray-200 flex items-center justify-between">
                  <span className="font-semibold text-gray-900">Route {idx + 1}</span>
                  <span className="text-sm text-gray-500 bg-white px-3 py-1 rounded-full border">
                    {route.transportationCount} transfer{route.transportationCount > 1 ? 's' : ''}
                  </span>
                </div>
                
                {/* Route Content */}
                <div className="p-5">
                  {/* Transportation Steps - Using 'segments' from backend */}
                  <div className="space-y-4">
                    {route.segments?.map((segment, tIdx) => (
                      <div key={tIdx} className="flex items-start gap-4">
                        {/* Icon */}
                        <div className={`w-10 h-10 rounded-full border flex items-center justify-center text-lg flex-shrink-0 ${getTypeStyle(segment.type)}`}>
                          {getTypeIcon(segment.type)}
                        </div>
                        
                        {/* Details */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1.5">
                            <span className="font-medium text-gray-900">{getTypeLabel(segment.type)}</span>
                          </div>
                          
                          <div className="flex items-center gap-2">
                            <span className="px-2.5 py-1 rounded-md text-sm font-semibold bg-blue-50 text-blue-700">
                              {segment.from?.locationCode}
                            </span>
                            
                            <svg className="w-5 h-5 text-gray-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
                            </svg>
                            
                            <span className="px-2.5 py-1 rounded-md text-sm font-semibold bg-green-50 text-green-700">
                              {segment.to?.locationCode}
                            </span>
                          </div>
                          
                          <div className="text-xs text-gray-500 mt-1.5">
                            {segment.from?.name} → {segment.to?.name}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  
                  {/* Route Summary */}
                  <div className="mt-5 pt-4 border-t border-gray-100 flex items-center justify-between text-sm">
                    <div className="text-gray-600">
                      <span className="text-gray-400">From:</span> {route.origin?.name}
                      <span className="mx-2 text-gray-300">|</span>
                      <span className="text-gray-400">To:</span> {route.destination?.name}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Empty State */}
      {!searching && routes.length === 0 && searchParams.originLocationId === '' && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-3xl">🗺️</span>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-1">Find your route</h3>
          <p className="text-gray-500">Select origin, destination and travel date to search</p>
        </div>
      )}

      {/* No Results */}
      {!searching && routes.length === 0 && searchParams.originLocationId !== '' && !error && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <div className="w-16 h-16 bg-yellow-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-3xl">🔍</span>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-1">No routes found</h3>
          <p className="text-gray-500">Try different locations or date</p>
        </div>
      )}
    </div>
  );
};

export default RoutesPage;
