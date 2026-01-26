import { useState, useEffect } from "react"

import axios from 'axios'

export function useTasks() {
    const [tasks, setTasks] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/v1/task')
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