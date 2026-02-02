import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import MainPage from './pages/MainPage'
import UpdateTask from './pages/UpdateTask'
import MainPortal from './pages/MainPortal'
import Login from './pages/Login'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path='/login' element={<Login/>} />
        <Route path='/' element={<MainPortal/>} />
        <Route path='/taskController' element={<MainPage />} />
        <Route path='/updateTask' element={<UpdateTask/>} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
)
