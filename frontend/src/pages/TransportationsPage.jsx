import { useState, useEffect } from 'react';
import { transportationsAPI, locationsAPI } from '../services/api';
import Modal from '../components/Modal';

const DAYS = [
  { value: 1, label: 'Mon' },
  { value: 2, label: 'Tue' },
  { value: 3, label: 'Wed' },
  { value: 4, label: 'Thu' },
  { value: 5, label: 'Fri' },
  { value: 6, label: 'Sat' },
  { value: 7, label: 'Sun' },
];

const TRANSPORTATION_TYPES = ['FLIGHT', 'BUS', 'SUBWAY', 'UBER'];

const TransportationsPage = () => {
  const [transportations, setTransportations] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTransportation, setEditingTransportation] = useState(null);
  const [formData, setFormData] = useState({
    originLocationId: '',
    destinationLocationId: '',
    transportationType: 'FLIGHT',
    operatingDays: [1, 2, 3, 4, 5],
  });

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(20);

  const fetchData = async (page = currentPage) => {
    try {
      setLoading(true);
      const [transRes, locRes] = await Promise.all([
        transportationsAPI.getAll(page, pageSize),
        locationsAPI.getAll(0, 1000),
      ]);
      const transPageData = transRes.data;
      setTransportations(transPageData.content);
      setTotalPages(transPageData.totalPages);
      setTotalElements(transPageData.totalElements);
      setCurrentPage(transPageData.number);
      setLocations(locRes.data.content);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(0);
  }, []);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchData(newPage);
    }
  };

  const handleOpenModal = (transportation = null) => {
    if (transportation) {
      setEditingTransportation(transportation);
      setFormData({
        originLocationId: transportation.originLocation.id,
        destinationLocationId: transportation.destinationLocation.id,
        transportationType: transportation.transportationType,
        operatingDays: transportation.operatingDays,
      });
    } else {
      setEditingTransportation(null);
      setFormData({
        originLocationId: '',
        destinationLocationId: '',
        transportationType: 'FLIGHT',
        operatingDays: [1, 2, 3, 4, 5],
      });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingTransportation(null);
  };

  const handleDayToggle = (day) => {
    const current = formData.operatingDays;
    if (current.includes(day)) {
      setFormData({
        ...formData,
        operatingDays: current.filter((d) => d !== day),
      });
    } else {
      setFormData({
        ...formData,
        operatingDays: [...current, day].sort(),
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingTransportation) {
        await transportationsAPI.update(editingTransportation.id, formData);
      } else {
        await transportationsAPI.create(formData);
      }
      handleCloseModal();
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this transportation?')) {
      return;
    }
    try {
      await transportationsAPI.delete(id);
      fetchData();
    } catch (err) {
      setError('Failed to delete transportation');
    }
  };

  const getTypeColor = (type) => {
    const colors = {
      FLIGHT: 'bg-blue-100 text-blue-700',
      BUS: 'bg-green-100 text-green-700',
      SUBWAY: 'bg-purple-100 text-purple-700',
      UBER: 'bg-orange-100 text-orange-700',
    };
    return colors[type] || 'bg-gray-100 text-gray-700';
  };

  const formatDays = (days) => {
    return days.map((d) => DAYS.find((day) => day.value === d)?.label).join(', ');
  };

  const getPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  };

  if (loading && transportations.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500">Loading transportations...</div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Transportations</h1>
          <p className="text-gray-500 mt-1">Manage flights, buses, and other transport options</p>
        </div>
        <button
          onClick={() => handleOpenModal()}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 shadow-sm"
        >
          <span>+</span> Add Transportation
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-4">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-200">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Type
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Origin
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Destination
              </th>
              <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Operating Days
              </th>
              <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {transportations.map((trans) => (
              <tr key={trans.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                  #{trans.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-3 py-1 rounded-md text-xs font-semibold ${getTypeColor(
                      trans.transportationType
                    )}`}
                  >
                    {trans.transportationType}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div className="font-semibold text-gray-900">
                      {trans.originLocation.locationCode}
                    </div>
                    <div className="text-xs text-gray-500">
                      {trans.originLocation.name}
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div className="font-semibold text-gray-900">
                      {trans.destinationLocation.locationCode}
                    </div>
                    <div className="text-xs text-gray-500">
                      {trans.destinationLocation.name}
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                  {formatDays(trans.operatingDays)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right">
                  <button
                    onClick={() => handleOpenModal(trans)}
                    className="text-blue-600 hover:text-blue-800 mr-4 font-medium"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(trans.id)}
                    className="text-red-600 hover:text-red-800 font-medium"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {transportations.length === 0 && !loading && (
          <div className="text-center py-12 text-gray-500">
            No transportations found. Add your first transportation!
          </div>
        )}
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex flex-col sm:flex-row items-center justify-between mt-6 gap-4">
          <div className="text-sm text-gray-600">
            Page {currentPage + 1} of {totalPages} ({totalElements} total items)
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => handlePageChange(0)}
              disabled={currentPage === 0}
              className="px-3 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              First
            </button>
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="px-3 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Prev
            </button>
            <div className="flex items-center gap-1">
              {getPageNumbers().map((page) => (
                <button
                  key={page}
                  onClick={() => handlePageChange(page)}
                  className={`px-3 py-2 rounded-lg transition-colors ${
                    page === currentPage
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'border border-gray-300 text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  {page + 1}
                </button>
              ))}
            </div>
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              className="px-3 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Next
            </button>
            <button
              onClick={() => handlePageChange(totalPages - 1)}
              disabled={currentPage === totalPages - 1}
              className="px-3 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Last
            </button>
          </div>
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={editingTransportation ? 'Edit Transportation' : 'Add Transportation'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Origin Location *
            </label>
            <select
              value={formData.originLocationId}
              onChange={(e) =>
                setFormData({ ...formData, originLocationId: parseInt(e.target.value) })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
              required
            >
              <option value="">Select origin location</option>
              {locations.map((loc) => (
                <option key={loc.id} value={loc.id}>
                  {loc.locationCode} - {loc.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Destination Location *
            </label>
            <select
              value={formData.destinationLocationId}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  destinationLocationId: parseInt(e.target.value),
                })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
              required
            >
              <option value="">Select destination location</option>
              {locations.map((loc) => (
                <option key={loc.id} value={loc.id}>
                  {loc.locationCode} - {loc.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Transportation Type *
            </label>
            <select
              value={formData.transportationType}
              onChange={(e) =>
                setFormData({ ...formData, transportationType: e.target.value })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
              required
            >
              {TRANSPORTATION_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Operating Days *
            </label>
            <div className="flex flex-wrap gap-2">
              {DAYS.map((day) => (
                <button
                  key={day.value}
                  type="button"
                  onClick={() => handleDayToggle(day.value)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    formData.operatingDays.includes(day.value)
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {day.label}
                </button>
              ))}
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={handleCloseModal}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              {editingTransportation ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default TransportationsPage;
