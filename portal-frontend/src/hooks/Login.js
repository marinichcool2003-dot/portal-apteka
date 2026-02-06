import { useState } from "react"
import api from './axios'

export function useLogin() {
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const [token, setToken] = useState(localStorage.getItem('token') || '')

    const login = async (username, password, rememberMe = false) => {
        setLoading(true)
        setError(null)
        try {
            const response = await api.post('/auth/login', {
                login: username,
                password: password
            })
            
            const { token } = response.data
            setToken(token)

            if (rememberMe) {
                localStorage.setItem('token', token)
            } else {
                sessionStorage.setItem('token', token)
                localStorage.removeItem('token')
            }

            api.defaults.headers.common['Authorization'] = `Bearer ${token}`

            return { success: true, token }
        } catch (err) {
            const errorMessage = err.response?.data?.message || 
                                err.message || 
                                'Ошибка авторизации'
            setError(errorMessage)
            return { success: false, error: errorMessage }
        } finally {
            setLoading(false)
        }
    }

    const logout = () => {
        setToken('')
        localStorage.removeItem('token')
        sessionStorage.removeItem('token')
        delete api.defaults.headers.common['Authorization']
    }

    const isAuthenticated = () => {
        return !!token || !!localStorage.getItem('token') || !!sessionStorage.getItem('token')
    }

    return {
        login,
        logout,
        isAuthenticated,
        loading,
        error,
        token
    }
}