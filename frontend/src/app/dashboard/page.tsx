"use client";

import { useQuery } from "@tanstack/react-query";
import { DollarSign, Package, TrendingUp, Users, Sparkles, Activity, BarChart3, Brain, Trophy } from "lucide-react";
import { AppShell } from "@/components/app-shell";
import { useAuthGuard } from "@/hooks/use-auth-guard";
import { dashboardStats } from "@/lib/api/crm";
import { money } from "@/lib/format";
import Link from "next/link";

export default function DashboardPage() {
  useAuthGuard();
  const { data, isLoading } = useQuery({ queryKey: ["dashboard-stats"], queryFn: dashboardStats });
  const cards = [
    { label: "Total Customers", value: data?.totalCustomers ?? 0, icon: Users },
    { label: "Total Orders", value: data?.totalOrders ?? 0, icon: Package },
    { label: "Revenue", value: money(data?.revenue), icon: DollarSign },
    { label: "Average Order Value", value: money(data?.averageOrderValue), icon: TrendingUp }
  ];

  return (
    <AppShell title="Dashboard" eyebrow="CRM Core">
      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <div key={card.label} className="rounded-lg border bg-card p-5">
            <card.icon className="h-5 w-5 text-primary" />
            <p className="mt-4 text-sm text-muted-foreground">{card.label}</p>
            <p className="mt-1 text-2xl font-semibold">{isLoading ? "-" : card.value}</p>
          </div>
        ))}
      </section>

      <section className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="rounded-lg border bg-card p-6">
          <div className="flex items-center gap-2 mb-4">
            <BarChart3 className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-semibold">Revenue Operations Snapshot</h2>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="rounded-md border p-4">
              <p className="text-sm text-muted-foreground">Customer Base</p>
              <p className="mt-2 text-2xl font-bold">{data?.totalCustomers ?? 0}</p>
            </div>

            <div className="rounded-md border p-4">
              <p className="text-sm text-muted-foreground">Order Volume</p>
              <p className="mt-2 text-2xl font-bold">{data?.totalOrders ?? 0}</p>
            </div>

            <div className="rounded-md border p-4">
              <p className="text-sm text-muted-foreground">Revenue</p>
              <p className="mt-2 text-2xl font-bold">{money(data?.revenue)}</p>
            </div>

            <div className="rounded-md border p-4">
              <p className="text-sm text-muted-foreground">Avg Order Value</p>
              <p className="mt-2 text-2xl font-bold">{money(data?.averageOrderValue)}</p>
            </div>
          </div>
        </div>

        <div className="rounded-lg border bg-card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Brain className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-semibold">AI Recommendation</h2>
          </div>

          <div className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">Revenue is concentrated among</p>
              <p className="text-2xl font-bold">12% of customers</p>
            </div>

            <div className="rounded-md border p-4 bg-muted/30">
              <p className="text-sm font-medium mb-2">Suggested Action</p>
              <p className="text-sm text-muted-foreground">
                Launch retention campaign for inactive customers.
              </p>
            </div>

            <Link
              href="/campaigns"
              className="inline-flex text-sm font-medium text-primary hover:underline"
            >
              Create Campaign →
            </Link>
          </div>
        </div>
      </section>

      <section className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="rounded-lg border bg-card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Activity className="h-5 w-5 text-primary" />
            <h2 className="font-semibold">Recent Activity</h2>
          </div>

          <div className="space-y-3 text-sm">
            <Link href="/customers" className="block text-primary hover:underline">
              Customer records updated
            </Link>
            <Link href="/orders" className="block text-primary hover:underline">
              Orders processed
            </Link>
            <Link href="/segments" className="block text-primary hover:underline">
              Segments generated
            </Link>
            <Link href="/campaigns" className="block text-primary hover:underline">
              Campaigns launched
            </Link>
          </div>
        </div>

        <div className="rounded-lg border bg-card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Brain className="h-5 w-5 text-primary" />
            <h2 className="font-semibold">AI Insights</h2>
          </div>

          <p className="text-sm text-muted-foreground">
            Audience intelligence and campaign recommendations generated through Groq-powered AI workflows.
          </p>
          <div className="mt-4">
            <Link href="/insights" className="text-primary hover:underline text-sm font-medium">
              Open Insights →
            </Link>
          </div>
        </div>

        <div className="rounded-lg border bg-card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Trophy className="h-5 w-5 text-primary" />
            <h2 className="font-semibold">Business Health</h2>
          </div>

          <div className="space-y-2 text-sm">
            <div>Customers ✓</div>
            <div>Orders ✓</div>
            <div>Campaigns ✓</div>
            <div>AI Audience ✓</div>
          </div>
          <div className="mt-4">
            <Link href="/analytics" className="text-primary hover:underline text-sm font-medium">
              View Analytics →
            </Link>
          </div>
        </div>
      </section>
    </AppShell>
  );
}
