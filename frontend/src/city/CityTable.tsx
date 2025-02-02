import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Pagination,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  TableSortLabel,
} from '@mui/material';

interface City {
  id: number;
  name: string;
  temperature: string;
}
enum Fields {
  name = 'name',
  temperature = 'temperature',
}
enum Sort {
  asc = 'asc',
  desc = 'desc',
}

const CitiesTable: React.FC = () => {
  const [cities, setCities] = useState<City[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(15);
  const [sortBy, setSortBy] = useState<Fields>(Fields.name);
  const [sortDirection, setSortDirection] = useState<Sort>(Sort.asc);

  useEffect(() => {
    axios
      .get(`/api/cities`, {
        params: {
          page,
          size: rowsPerPage,
          sort: `${sortBy},${sortDirection}`,
        },
      })
      .then((response) => {
        (response.data.content as City[]).forEach(
          (city) =>
            (city.temperature = (Number(city.temperature) - 273.15).toFixed(1))
        );
        setCities(response.data.content);
        setTotalPages(response.data.totalPages);
      })
      // throw error as popup etc
      .catch((error) => console.error(error));
  }, [page, rowsPerPage, sortBy, sortDirection]);

  const handleSort = (key: Fields) => {
    const isAsc = sortBy === key && sortDirection === Sort.asc;
    setSortDirection(isAsc ? Sort.desc : Sort.asc);
    setSortBy(key);
    setPage(0);
  };

  return (
    <div style={{ padding: '20px' }}>
      <TableContainer
        component={Paper}
        sx={{
          flex: 1,
          overflow: 'auto',
          maxHeight: '80vh',
          '&::-webkit-scrollbar': { width: '8px' },
          '&::-webkit-scrollbar-thumb': { backgroundColor: 'grey.500' },
        }}
      >
        <Table sx={{ tableLayout: 'fixed' }}>
          <TableHead
            sx={{
              position: 'sticky',
              top: 0,
              backgroundColor: 'background.paper',
              zIndex: 1,
            }}
          >
            <TableRow>
              <TableCell
                sx={{
                  width: { xs: '60%', sm: '70%' },
                  borderRight: '1px solid',
                  borderColor: 'divider',
                  '& .MuiTableSortLabel-icon': {
                    position: 'absolute',
                    left: -20,
                  },
                }}
              >
                <TableSortLabel
                  active={sortBy === Fields.name}
                  direction={sortDirection === Sort.asc ? 'asc' : 'desc'}
                  onClick={() => handleSort(Fields.name)}
                >
                  City
                </TableSortLabel>
              </TableCell>
              <TableCell
                sx={{
                  width: { xs: '40%', sm: '30%' },
                  '& .MuiTableSortLabel-icon': {
                    position: 'absolute',
                    left: -20,
                  },
                }}
              >
                <TableSortLabel
                  active={sortBy === Fields.temperature}
                  direction={sortDirection === Sort.asc ? 'asc' : 'desc'}
                  onClick={() => handleSort(Fields.temperature)}
                >
                  Temperature (°C)
                </TableSortLabel>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cities.map((city) => (
              <TableRow key={city.id}>
                <TableCell
                  sx={{
                    width: { xs: '60%', sm: '70%' },
                    borderRight: '1px solid',
                    borderColor: 'divider',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {city.name}
                </TableCell>
                <TableCell
                  sx={{
                    width: { xs: '40%', sm: '30%' },
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  {city.temperature}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            padding: '20px',
            flexWrap: 'wrap',
            position: 'sticky',
            bottom: 0,
            backgroundColor: 'white',
            zIndex: 1,
          }}
        >
          <Pagination
            count={totalPages}
            page={page + 1}
            siblingCount={0}
            onChange={(_, newPage) => setPage(newPage - 1)}
            sx={{ display: 'flex', py: 2 }}
          />
          <FormControl
            variant="standard"
            sx={{ minWidth: 100, display: 'flex', justifyContent: 'center' }}
          >
            <InputLabel>Rows per page</InputLabel>
            <Select
              value={rowsPerPage}
              onChange={(e) => setRowsPerPage(e.target.value as number)}
            >
              <MenuItem value={15}>15</MenuItem>
              <MenuItem value={25}>25</MenuItem>
            </Select>
          </FormControl>
        </div>
      </TableContainer>
    </div>
  );
};

export default CitiesTable;
