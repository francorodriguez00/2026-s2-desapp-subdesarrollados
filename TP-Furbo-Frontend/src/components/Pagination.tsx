import React, { useState } from 'react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
  const [inputPage, setInputPage] = useState('');

  const handleJump = (e: React.FormEvent) => {
    e.preventDefault();
    const pageNum = parseInt(inputPage);
    if (!isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages) {
      onPageChange(pageNum - 1);
      setInputPage('');
    }
  };

  return (
    <div style={{ marginTop: '20px', display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
      <button disabled={currentPage === 0} onClick={() => onPageChange(0)}>Inicio</button>
      <button disabled={currentPage === 0} onClick={() => onPageChange(currentPage - 1)}>Anterior</button>
      
      <span>Página {currentPage + 1} de {totalPages}</span>
      
      <button disabled={currentPage >= totalPages - 1} onClick={() => onPageChange(currentPage + 1)}>Siguiente</button>
      <button disabled={currentPage >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)}>Final</button>
      
      <form onSubmit={handleJump} style={{ marginLeft: '10px' }}>
        <input 
          type="number" 
          value={inputPage} 
          onChange={(e) => setInputPage(e.target.value)} 
          placeholder="Saltar a..." 
          style={{ width: '80px' }}
        />
        <button type="submit">Ir</button>
      </form>
    </div>
  );
};
