import { useState, useEffect } from "react"

import api from './axios.js'

export function useTasks() {
    const [tasks, setTasks] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                const response = await api.get('/task')
                console.log(response.data)
                setTasks(response.data)
            } catch (err) {
                setError(err.message || 'Произошла ошибка при попытке загрузить задачи')
            } finally {
                setLoading(false)
            }
        }
        fetchTasks()
    }, [])
    return {tasks, loading, error}
}