import React, { useEffect, useState } from 'react';
import { authService } from '../../services/authService';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { DatePicker } from '@/components/ui/date-picker';
import { Textarea } from '@/components/ui/textarea';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { getDateFilterHorasExtra } from '@/lib/utils';
import { toast } from 'sonner';

type Role = 'ADMIN' | 'HR' | 'JEFE' | 'EMPLEADO';

interface HorasExtraDTO {
  id?: number;
  fechaSolicitud: string;
  cantidadDeHoras: number;
  motivo: string;
  aprobado?: boolean;
  procesado?: boolean;
  estadoSolicitud?: string;
  tipoTarifa: string;
  idEmpleado?: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
}

const API_BASE = 'http://localhost:8080';

export default function MisHorasExtraView() {
  function formatoEstado(s?: string) {
    switch (s) {
      case 'PENDIENTE':
        return 'Pendiente';
      case 'PENDIENTE_RH':
        return 'Pendiente RH';
      case 'APROBADA_POR_JEFE':
        return 'Aprobada por jefe';
      case 'RECHAZADA_POR_JEFE':
        return 'Rechazada por jefe';
      case 'RECHAZADA_POR_RH':
        return 'Rechazada por RH';
      case 'APROBADA':
        return 'Aprobada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return s || '';
    }
  }

  const [fecha, setFecha] = useState<string>(new Date().toISOString().slice(0, 10));
  const [horas, setHoras] = useState<number>(1);
  const [motivo, setMotivo] = useState<string>('');
  const [tipo, setTipo] = useState<string>('SIMPLE');
  const [lista, setLista] = useState<HorasExtraDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const userInfo = authService.getUserInfo();
  const role = (userInfo.role || 'EMPLEADO') as Role;
  const token = localStorage.getItem('token') || '';

  useEffect(() => {
    fetchLista();
  }, []);

  async function fetchLista() {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE}/api/horas-extra`, {
        headers: { Authorization: token ? `Bearer ${token}` : '' },
      });
      if (!res.ok) throw new Error('Error al obtener solicitudes');
      const data = await res.json();
      const items: any[] = Array.isArray(data) ? data : Array.isArray((data as any)?.content) ? (data as any).content : [];
      const myId = userInfo.idEmpleado;
      const filtered = items.filter((d: any) => d.idEmpleado === myId);
      setLista(filtered || []);
      setPage(0);
    } catch (e) {
      console.error(e);
      toast.error('No se pudo cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      const idEmpleado = userInfo.idEmpleado;

      if (!idEmpleado) {
        toast.error('No se pudo determinar el empleado autenticado. Inicie sesión nuevamente.');
        return;
      }

      const payload = {
        fechaSolicitud: fecha,
        cantidadDeHoras: horas,
        motivo,
        tipoTarifa: tipo,
        aprobado: false,
        procesado: false,
        estadoSolicitud: 'PENDIENTE',
        idEmpleado: idEmpleado,
      };
      const res = await fetch(`${API_BASE}/api/horas-extra/solicitar`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Error en la solicitud');
      }
      toast.success('Solicitud enviada');
      setMotivo('');
      setHoras(1);
      fetchLista();
    } catch (err: any) {
      toast.error(err.message || 'Error');
    }
  }

  const totalPages = Math.ceil(lista.length / pageSize);
  const startIndex = page * pageSize;
  const paginatedSolicitudes = lista.slice(startIndex, startIndex + pageSize);

  useEffect(() => {
    if (totalPages > 0 && page >= totalPages) {
      setPage(Math.max(totalPages - 1, 0));
    }
  }, [page, totalPages]);

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0);
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Mis Horas Extra</CardTitle>
          <CardDescription>Solicita hasta 3 horas extra para hoy o ayer.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <Label>Fecha</Label>
              <DatePicker
                value={fecha}
                onChange={(v: string) => setFecha(v)}
                placeholder="Seleccionar fecha"
                filterDate={getDateFilterHorasExtra()}
                fromYear={new Date().getFullYear() - 1}
                toYear={new Date().getFullYear()}
              />
            </div>

            <div className="space-y-1">
              <Label>Horas (max 3)</Label>
              <Input type="number" min={1} max={3} value={horas} onChange={(e) => setHoras(Number(e.target.value))} />
            </div>

            <div className="md:col-span-2 space-y-1">
              <Label>Motivo</Label>
              <Textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} />
            </div>

            <div className="md:col-span-2 flex justify-end">
              <Button type="submit">Enviar solicitud</Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Mis Solicitudes</CardTitle>
          <CardDescription>Lista de mis solicitudes de horas extra</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center p-8">Cargando...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm table-fixed">
                <thead>
                  <tr className="text-left text-muted-foreground">
                    <th className="py-2 px-3 w-32">Fecha</th>
                    <th className="py-2 px-3 w-24">Horas</th>
                    <th className="py-2 px-3 w-48">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedSolicitudes.map((item) => (
                    <tr key={item.id} className="border-t">
                      <td className="py-2 px-3">{item.fechaSolicitud}</td>
                      <td className="py-2 px-3">{item.cantidadDeHoras}</td>
                      <td className="py-2 px-3">{formatoEstado(item.estadoSolicitud)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {lista.length > 0 && (
            <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">Mostrar:</span>
                <Select value={String(pageSize)} onValueChange={handlePageSizeChange}>
                  <SelectTrigger className="w-[110px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="5">5</SelectItem>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {totalPages > 1 && (
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage(Math.max(0, page - 1))}
                        className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>

                    {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => {
                      if (
                        pageNum === 0 ||
                        pageNum === totalPages - 1 ||
                        (pageNum >= page - 1 && pageNum <= page + 1)
                      ) {
                        return (
                          <PaginationItem key={pageNum}>
                            <PaginationLink
                              onClick={() => setPage(pageNum)}
                              isActive={pageNum === page}
                              className="cursor-pointer"
                            >
                              {pageNum + 1}
                            </PaginationLink>
                          </PaginationItem>
                        );
                      }
                      if (pageNum === page - 2 || pageNum === page + 2) {
                        return (
                          <PaginationItem key={pageNum}>
                            <span className="flex h-9 w-9 items-center justify-center">...</span>
                          </PaginationItem>
                        );
                      }
                      return null;
                    })}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                        className={page === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              )}

              <div className="text-sm text-muted-foreground text-center sm:text-right">
                {totalPages > 0
                  ? `Página ${page + 1} de ${totalPages} • ${lista.length} solicitud(es)`
                  : 'Sin solicitudes para paginar'}
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
