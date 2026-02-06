import { useState, useEffect } from "react";

import api from './axios.js'

export function useClientGroups() {
    const [groups, setGroups] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchClientGroups = async () => {
            try {
                const response = await api.get('/group-client')
                console.log(response.data)
                setGroups(response.data)
            } catch (err) {
                setError(err.message || 'произошла ошибка при получении групп пользователей')
            } finally {
                setLoading(false)
            }
        }
        fetchClientGroups()
    }, [])
    return {groups, loading, error}
}