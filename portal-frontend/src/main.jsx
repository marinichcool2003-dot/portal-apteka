import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import MainPage from './pages/MainPage'
import UpdateTask from './pages/UpdateTask'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<MainPage />} />
        <Route path='/updateTask' element={<UpdateTask/>} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
