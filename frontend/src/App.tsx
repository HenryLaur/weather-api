import React from 'react';
import CityTable from './city/CityTable.tsx';

const App: React.FC = () => {
  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
      <h1 style={{ textAlign: 'center', marginBottom: '30px' }}>
        City Temperatures
      </h1>
      <CityTable />
    </div>
  );
};

export default App; // Make sure to include this export
